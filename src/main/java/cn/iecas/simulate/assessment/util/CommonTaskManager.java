package cn.iecas.simulate.assessment.util;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.*;

import java.util.Date;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * 通用任务管理工具
 * - 普通执行线程，启动即执行，直到任务结束
 * - 固定时间定时任务
 * - cron定时任务
 */
public class CommonTaskManager {

    // 任务状态枚举
    public enum TaskState {
        CREATED, RUNNING, PAUSED, STOPPED, COMPLETED, FAILED
    }

    /**
     * 任务接口（添加暂停检查点）
     */
    public interface ManagedTask {
        /**
         * 执行任务，支持暂停检查
         *
         * @param pauseChecker 暂停检查器，用于在任务中检查暂停状态
         */
        void execute(PauseChecker pauseChecker) throws Exception;
    }

    /**
     * 暂停检查器接口
     */
    public interface PauseChecker {
        /**
         * 检查任务是否被暂停，如果暂停则阻塞
         */
        void checkPaused() throws InterruptedException;
    }

    /**
     * 可控制的任务封装
     */
    public static class TaskController implements PauseChecker {
        private final String taskId;
        private volatile TaskState state = TaskState.CREATED;
        private final Lock pauseLock = new ReentrantLock();
        private final Condition pauseCondition = pauseLock.newCondition();
        private final AtomicBoolean paused = new AtomicBoolean(false);
        private final AtomicBoolean stopped = new AtomicBoolean(false);
        private ScheduledFuture<?> scheduledFuture;
        private Thread executionThread; // ==================== 新增：记录执行线程 ====================
        private String cronExpression;
        private SimpleTriggerContext triggerContext;

        public TaskController(String taskId) {
            this.taskId = taskId;
            this.triggerContext = new SimpleTriggerContext();
        }

        public void setCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        /**
         * 检查并处理暂停状态
         */
        @Override
        public void checkPaused() throws InterruptedException {
            if (!paused.get()) return;

            pauseLock.lock();
            try {
                state = TaskState.PAUSED;
                // ==================== 记录当前执行线程 ====================
                executionThread = Thread.currentThread();
                while (paused.get()) {
                    pauseCondition.await();
                }
                state = TaskState.RUNNING;
            } finally {
                executionThread = null; // ==================== 清除线程引用 ====================
                pauseLock.unlock();
            }
        }

        public void pause() {
            paused.set(true);
            // ==================== 新增：中断执行线程 ====================
            if (executionThread != null) {
                executionThread.interrupt();
            }
        }

        public void resume() {
            pauseLock.lock();
            try {
                paused.set(false);
                pauseCondition.signalAll();
            } finally {
                pauseLock.unlock();
            }
        }

        public void stop() {
            stopped.set(true);
            if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
            // ==================== 新增：中断执行线程 ====================
            if (executionThread != null) {
                executionThread.interrupt();
            }
            // 如果暂停中，唤醒线程
            if (paused.get()) {
                resume();
            }
        }

        public TaskState getState() {
            return state;
        }

        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }
    }

    // 任务调度器
    private ScheduledExecutorService scheduledExecutor;
    private ThreadPoolExecutor taskExecutor;
    // 任务存储
    private final ConcurrentMap<String, TaskController> taskControllers = new ConcurrentHashMap<>();
    // 任务实现存储
    private final ConcurrentMap<String, ManagedTask> managedTasks = new ConcurrentHashMap<>();
    // 任务队列监控器
    private TaskQueueMonitor queueMonitor;

    /**
     * 构造函数
     */
    public CommonTaskManager(int corePoolSize, int maxPoolSize, long keepAliveTime,
                                int queueCapacity, RejectedExecutionHandler rejectedHandler) {
        // 创建任务执行线程池
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.taskExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                Executors.defaultThreadFactory(),
                rejectedHandler
        );

        // 创建定时任务调度器
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);

        // 创建队列监控器
        this.queueMonitor = new TaskQueueMonitor(taskExecutor);

        // 启动监控线程
        new Thread(queueMonitor::monitorQueue).start();
    }

    /**
     * 创建任务
     */
    public void createTask(String taskId, ManagedTask task) {
        TaskController controller = new TaskController(taskId);
        if (taskControllers.putIfAbsent(taskId, controller) != null) {
            throw new IllegalArgumentException("Task ID already exists: " + taskId);
        }
        managedTasks.put(taskId, task);
    }

    /**
     * 创建定时任务（固定延迟）
     */
    public void createScheduledTask(String taskId, ManagedTask task, long delay) {
        createTask(taskId, task);
        scheduleAtFixedDelay(taskId, delay);
    }

    /**
     * 创建定时任务（Cron表达式）
     */
    public void createCronTask(String taskId, ManagedTask task, String cronExpression) {
        createTask(taskId, task);
        scheduleWithCron(taskId, cronExpression);
    }

    /**
     * 启动非定时任务
     */
    public void startTask(String taskId) {
        TaskController controller = taskControllers.get(taskId);
        if (controller == null) throw new IllegalArgumentException("Task not found");

        // 立即执行任务
        controller.setScheduledFuture(null);
        taskExecutor.execute(() -> executeTask(taskId, controller));
    }

    /**
     * 使用固定延迟调度
     */
    public void scheduleAtFixedDelay(String taskId, long delay) {
        TaskController controller = taskControllers.get(taskId);
        if (controller == null) throw new IllegalArgumentException("Task not found");

        controller.setScheduledFuture(scheduledExecutor.scheduleWithFixedDelay(
                () -> taskExecutor.execute(() -> executeTask(taskId, controller)),
                0, // 初始延迟
                delay,
                TimeUnit.MILLISECONDS
        ));
    }

    /**
     * 使用Cron表达式调度
     */
    public void scheduleWithCron(String taskId, String cronExpression) {
        TaskController controller = taskControllers.get(taskId);
        if (controller == null) throw new IllegalArgumentException("Task not found");

        controller.setCronExpression(cronExpression);

        // 创建Cron触发器
        CronTrigger cronTrigger = new CronTrigger(cronExpression);

        // 使用触发器上下文计算执行时间
        Date nextExecutionTime = cronTrigger.nextExecutionTime(controller.triggerContext);
        long delay = Math.max(0, nextExecutionTime.getTime() - System.currentTimeMillis());

        // 使用ScheduledExecutorService调度
        controller.setScheduledFuture(scheduledExecutor.schedule(
                () -> {
                    // 计算下次执行时间
                    Date nextExecutTime = cronTrigger.nextExecutionTime(controller.triggerContext);
                    // 提交任务到线程池
                    taskExecutor.execute(() -> executeTask(taskId, controller));
                    // 调度下次执行
                    scheduleNextCronExecution(taskId, nextExecutTime, controller);
                },
                delay,
                TimeUnit.MILLISECONDS
        ));
    }

    /**
     * 调度下一次Cron执行
     */
    private void scheduleNextCronExecution(String taskId, Date nextExecutTime, TaskController controller) {
        if (controller == null || controller.stopped.get()) return;

        // 计算下次执行时间
        long delay = Math.max(0, nextExecutTime.getTime() - System.currentTimeMillis());

        // 调度下次执行
        controller.setScheduledFuture(scheduledExecutor.schedule(
                () -> {
                    // 更新触发器上下文
                    Date actualExecutionTime = new Date();
                    controller.triggerContext.update(nextExecutTime, actualExecutionTime, actualExecutionTime);

                    // 计算新的下次执行时间
                    CronTrigger cronTrigger = new CronTrigger(controller.cronExpression);
                    Date newNextExecutionTime = cronTrigger.nextExecutionTime(controller.triggerContext);
                    taskExecutor.execute(() -> executeTask(taskId, controller));
                    scheduleNextCronExecution(taskId, newNextExecutionTime, controller);
                },
                delay,
                TimeUnit.MILLISECONDS
        ));
    }

    /**
     * 执行任务（带控制） - 修改：传递TaskController
     */
    private void executeTask(String taskId, TaskController controller) {
        // 任务执行前检查队列状态
        queueMonitor.checkQueueStatus();

        ManagedTask task = managedTasks.get(taskId);

        if (controller != null && task != null && !controller.stopped.get()) {
            try {
                controller.state = TaskState.RUNNING;
                // ==================== 关键修改：传递pauseChecker ====================
                task.execute(controller);
                if (!controller.stopped.get()) {
                    controller.state = TaskState.COMPLETED;
                }
            } catch (InterruptedException e) {
                // 被中断，可能是暂停或停止
                Thread.currentThread().interrupt();
                if (controller.paused.get()) {
                    controller.state = TaskState.PAUSED;
                } else {
                    controller.state = TaskState.STOPPED;
                }
            } catch (Exception e) {
                controller.state = TaskState.FAILED;
                e.printStackTrace();
            }
        }
    }

    /**
     * 暂停任务
     */
    public void pauseTask(String taskId) {
        TaskController controller = taskControllers.get(taskId);
        if (controller == null) throw new IllegalArgumentException("Task not found");

        controller.pause();
    }

    /**
     * 恢复任务
     */
    public void resumeTask(String taskId) {
        TaskController controller = taskControllers.get(taskId);
        if (controller == null) throw new IllegalArgumentException("Task not found");

        controller.resume();
    }

    /**
     * 停止任务
     */
    public void stopTask(String taskId) {
        TaskController controller = taskControllers.get(taskId);
        if (controller == null) throw new IllegalArgumentException("Task not found");

        controller.stop();
    }

    /**
     * 删除任务
     */
    public void deleteTask(String taskId) {
        stopTask(taskId);
        taskControllers.remove(taskId);
        managedTasks.remove(taskId);
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
        TaskController controller = taskControllers.get(taskId);
        return controller != null ? controller.getState() : null;
    }

    /**
     * 获取线程池队列大小
     */
    public int getQueueSize() {
        return taskExecutor.getQueue().size();
    }

    /**
     * 获取活跃线程数
     */
    public int getActiveThreadCount() {
        return taskExecutor.getActiveCount();
    }

    /**
     * 关闭任务管理器
     */
    public void shutdown() {
        // 停止所有任务
        taskControllers.values().forEach(controller -> controller.stop());
        taskControllers.clear();
        managedTasks.clear();

        // 关闭线程池
        scheduledExecutor.shutdownNow();
        taskExecutor.shutdown();

        try {
            if (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 任务队列监控器
     */
    private class TaskQueueMonitor {
        private final ThreadPoolExecutor executor;
        private static final int HIGH_QUEUE_THRESHOLD = 100;
        private static final int CRITICAL_QUEUE_THRESHOLD = 200;

        public TaskQueueMonitor(ThreadPoolExecutor executor) {
            this.executor = executor;
        }

        /**
         * 检查队列状态，必要时采取行动
         */
        public void checkQueueStatus() {
            int queueSize = executor.getQueue().size();
            int activeThreads = executor.getActiveCount();
            int poolSize = executor.getPoolSize();

            if (queueSize > CRITICAL_QUEUE_THRESHOLD) {
                handleCriticalQueue(queueSize, activeThreads, poolSize);
            } else if (queueSize > HIGH_QUEUE_THRESHOLD) {
                handleHighQueue(queueSize, activeThreads, poolSize);
            }
        }

        private void handleHighQueue(int queueSize, int activeThreads, int poolSize) {
            System.out.println("[WARN] High task queue: " + queueSize +
                    " tasks waiting. Active threads: " + activeThreads +
                    "/" + poolSize);
        }

        private void handleCriticalQueue(int queueSize, int activeThreads, int poolSize) {
            System.err.println("[ERROR] Critical task queue: " + queueSize +
                    " tasks waiting. Active threads: " + activeThreads +
                    "/" + poolSize);
        }

        /**
         * 监控队列
         */
        public void monitorQueue() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int queueSize = executor.getQueue().size();
                    int activeThreads = executor.getActiveCount();
                    int poolSize = executor.getPoolSize();

                    System.out.println("[Monitor] Queue size: " + queueSize +
                            ", Active threads: " + activeThreads +
                            "/" + poolSize);

                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }


    public void printTaskManagerInfo() {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("--任务信息：" + this.taskControllers.size());
        for (Map.Entry<String, TaskController> entity : this.taskControllers.entrySet()) {
            TaskController value = entity.getValue();
            System.out.println("Task [" + entity.getKey() + "] , 状态：" + value.getState());
        }
        System.out.println("--已完成：" + this.taskExecutor.getCompletedTaskCount());
        System.out.println("--线程池信息：" + this.getActiveThreadCount());
        System.out.println("--队列信息：" + this.getQueueSize());
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");
    }
}