package cn.iecas.simulate.assessment.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 异步任务管理工具
 *  - 普通执行线程，启动即执行，直到任务结束
 */
public class AsyncTaskManager {

    {
        this.executor = new ThreadPoolExecutor(
                2,
                5,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }


    // 任务状态枚举
    public enum TaskState {
        CREATED, RUNNING, PAUSED, STOPPED, COMPLETED, FAILED
    }

    /**
     * 可暂停的异步任务接口
     */
    public interface PausableTask {
        /**
         * 执行一个工作单元
         *
         * @return true 如果还有更多工作要做，false 如果任务完成
         * @throws Exception 执行中可能出现的异常
         */
        boolean executeWorkUnit() throws Exception;
    }

    /**
     * 异步任务封装类
     */
    public static class AsyncTask {
        private final String taskId;
        private volatile TaskState state = TaskState.CREATED;
        private final PausableTask task;
        private Future<?> future;
        private final Lock pauseLock = new ReentrantLock();
        private final Condition pauseCondition = pauseLock.newCondition();
        private final AtomicBoolean paused = new AtomicBoolean(false);
        private final AtomicBoolean stopped = new AtomicBoolean(false);

        public AsyncTask(String taskId, PausableTask task) {
            this.taskId = taskId;
            this.task = task;
        }

        /**
         * 任务执行逻辑（内部封装）
         */
        private void execute() {
            try {
                state = TaskState.RUNNING;

                while (!stopped.get() && task.executeWorkUnit()) {
                    // 检查暂停状态
                    checkPaused();

                    // 检查停止状态
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Task interrupted");
                    }
                }

                if (!stopped.get()) {
                    state = TaskState.COMPLETED;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                state = TaskState.STOPPED;
            } catch (Exception e) {
                state = TaskState.FAILED;
                throw new RuntimeException("Task execution failed", e);
            }
        }

        /**
         * 检查并处理暂停状态
         */
        private void checkPaused() throws InterruptedException {
            if (!paused.get()) return;

            pauseLock.lock();
            try {
                state = TaskState.PAUSED;
                while (paused.get()) {
                    pauseCondition.await();
                }
                state = TaskState.RUNNING;
            } finally {
                pauseLock.unlock();
            }
        }

        public String getTaskId() {
            return taskId;
        }

        public TaskState getState() {
            return state;
        }
    }

    // 线程池配置
    private ThreadPoolExecutor executor;
    // 任务存储
    private final ConcurrentMap<String, AsyncTask> tasks = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param corePoolSize    核心线程数
     * @param maxPoolSize     最大线程数
     * @param keepAliveTime   空闲线程存活时间
     * @param queueCapacity   队列容量
     * @param rejectedHandler 拒绝策略
     */
    public AsyncTaskManager(int corePoolSize, int maxPoolSize, long keepAliveTime,
                            int queueCapacity, RejectedExecutionHandler rejectedHandler) {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                Executors.defaultThreadFactory(),
                rejectedHandler
        );
    }

    /**
     * 创建任务
     */
    public void createTask(String taskId, PausableTask task) {
        AsyncTask asyncTask = new AsyncTask(taskId, task);
        if (tasks.putIfAbsent(taskId, asyncTask) != null) {
            throw new IllegalArgumentException("Task ID already exists: " + taskId);
        }
    }

    /**
     * 启动任务
     */
    public void startTask(String taskId) {
        AsyncTask asyncTask = tasks.get(taskId);
        if (asyncTask == null) throw new IllegalArgumentException("Task not found");
        if (asyncTask.state != TaskState.CREATED && asyncTask.state != TaskState.STOPPED) {
            throw new IllegalStateException("Task can only be started from CREATED or STOPPED state");
        }

        asyncTask.stopped.set(false);
        asyncTask.future = executor.submit(asyncTask::execute);
    }

    /**
     * 暂停任务
     */
    public void pauseTask(String taskId) {
        AsyncTask asyncTask = tasks.get(taskId);
        if (asyncTask == null) throw new IllegalArgumentException("Task not found");
        if (asyncTask.state != TaskState.RUNNING) {
            throw new IllegalStateException("Only running tasks can be paused");
        }
        asyncTask.state = TaskState.PAUSED;
        asyncTask.paused.set(true);
    }

    /**
     * 恢复任务
     */
    public void resumeTask(String taskId) {
        AsyncTask asyncTask = tasks.get(taskId);
        if (asyncTask == null) throw new IllegalArgumentException("Task not found");
        if (asyncTask.state != TaskState.PAUSED) {
            throw new IllegalStateException("Only paused tasks can be resumed");
        }

        asyncTask.pauseLock.lock();
        try {
            asyncTask.paused.set(false);
            asyncTask.pauseCondition.signalAll();
        } finally {
            asyncTask.pauseLock.unlock();
        }
    }

    /**
     * 停止任务
     */
    public void stopTask(String taskId) {
        AsyncTask asyncTask = tasks.get(taskId);
        if (asyncTask == null) throw new IllegalArgumentException("Task not found");

        asyncTask.stopped.set(true);
        if (asyncTask.future != null) {
            asyncTask.future.cancel(true);
        }
        asyncTask.state = TaskState.STOPPED;
    }

    /**
     * 删除任务
     */
    public void deleteTask(String taskId) {
        AsyncTask asyncTask = tasks.remove(taskId);
        if (asyncTask != null) {
            stopTask(taskId);
        }
    }

    /**
     * 重启任务
     */
    public void restartTask(String taskId) {
        stopTask(taskId);
        startTask(taskId);
    }

    /**
     * 获取任务状态
     */
    public TaskState getTaskState(String taskId) {
        AsyncTask asyncTask = tasks.get(taskId);
        return (asyncTask != null) ? asyncTask.state : null;
    }

    /**
     * 关闭任务管理器（释放资源）
     */
    public void shutdown() throws InterruptedException {
        while (true) {
            Thread.sleep(3000);
            if (this.getQueueSize() == 0 && this.getActiveThreadCount() == 0) {
                // 停止所有任务
                tasks.keySet().forEach(this::stopTask);
                tasks.clear();

                // 关闭线程池
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            } else {
                System.out.println("关闭线程持续监听中..." + executor.getActiveCount());
            }

        }
    }

    // 线程池监控方法
    public int getActiveThreadCount() {
        return executor.getActiveCount();
    }

    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    public int getQueueSize() {
        return executor.getQueue().size();
    }

    /**
     * 将Runnable转换为PausableTask的适配器
     */
    public static PausableTask adapt(Runnable runnable) {
        return new PausableTask() {
            private boolean executed = false;

            @Override
            public boolean executeWorkUnit() throws Exception {
                if (executed) return false;

                runnable.run();
                executed = true;
                return false;
            }
        };
    }


    public void printTaskManagerInfo() {
        System.out.println("------------------------------------------");
        System.out.println("--任务信息：" + this.tasks.size());
        for (Map.Entry<String, AsyncTask> stringAsyncTaskEntry : this.tasks.entrySet()) {
            AsyncTask value = stringAsyncTaskEntry.getValue();
            System.out.println("Task [" + stringAsyncTaskEntry.getKey() + "] , 状态：" + value.getState());
        }
        System.out.println("--已完成：" + this.getCompletedTaskCount());
        System.out.println("--线程池信息：" + this.getActiveThreadCount());
        System.out.println("--队列信息：" + this.getQueueSize());
    }

    public AsyncTaskManager() {
    }
}