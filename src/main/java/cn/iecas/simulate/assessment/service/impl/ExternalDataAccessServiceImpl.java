package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.domain.*;
import cn.iecas.simulate.assessment.dao.AssessmentStatisticDao;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.entity.model.emun.ModelType;
import cn.iecas.simulate.assessment.service.*;
import cn.iecas.simulate.assessment.service.model.ModelTypeService;
import cn.iecas.simulate.assessment.service.model.impl.ModelCommonServiceImpl;
import cn.iecas.simulate.assessment.util.MathUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import manager.ThreadManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * @Time: 2024/10/9 10:36
 * @Author: guoxun
 * @File: ExternalDataAccessServiceImpl
 * @Description: 外部数据接引接口实现类
 */
@Service
@Slf4j
public class ExternalDataAccessServiceImpl implements ExternalDataAccessService {

    /**
     * 内部类，用于存储每个模型所对应任务的信息
     */
    @Data
    public static class StatusInfo {

        /**
         * 父任务id taskId
         */
        public Integer parentTaskId;

        /**
         * 模型名称
         */
        public String modelName;

        /**
         * 模型id
         */
        public Integer modelId;

        /**
         * 完成状态, true 已完成； false 未完成 默认：false
         */
        public Boolean isAchieve = false;

        /**
         * 当前任务的当前模型的任务完成量 默认为0
         */
        public Integer achieveCount = 0;

        /**
         * 需要完成的组数
         */
        public Integer groupCount;

        /**
         * 数据引接方式
         */
        public String way;

        /**
         * 每次引接个数
         */
        public Integer pageSize;

        /**
         * 对应查询条件
         */
        public ExternalDataDTO dto;

        /**
         * tb_model_assessment_info 数据库更新标志位
         */
        public Boolean MAIFlag = false;
    }

    @Autowired
    private SimulateTaskService simulateTaskService;

    @Autowired
    private ModelAssessmentService assessmentService;

    @Autowired
    private RestTemplateApi templateApi;

    @Autowired
    private SimulateTaskDao taskDao;

    @Autowired
    private ModelService modelService;

    @Autowired
    private ModelCommonServiceImpl commonService;

    @Autowired
    private ModelRunDataService runDataService;

    @Value("${external-data-access.frequency}")
    private Integer frequency;

    @Value("${external-data-access.pageSize}")
    private Integer pageSize;

    @Value("${external-data-access.groupCount}")
    private Integer groupCount;

    @Value("${external-data-access.way}")
    private String way;

    @Value("${external-data-access.base}")
    private Integer base;

    @Value("${external-data-access.default-request-url}")
    private String defaultRequestUrl;

    @Value("${external-data-access.thread-setting.max}")
    private int maxThreads;

    @Value("${external-data-access.use-test}")
    private boolean useTest;

    public static final ConcurrentHashMap<String, Thread> threads = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, ExternalDataDTO> dtoMap = new ConcurrentHashMap<>();

    public static final AtomicInteger currentThreadCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, Boolean> isSuspendMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, String> cacheMap = new ConcurrentHashMap<>();

    /**
     * 缓存
     */
    private static final ConcurrentHashMap<Integer, List<StatusInfo>> statusInfoListMap = new ConcurrentHashMap<>();

    /**
     * 模型运行数据缓存
     */
    public static final ConcurrentHashMap<String, JSONObject> realDataPool = new ConcurrentHashMap<>();


    @Override
    public List<SimulateDataInfo> getExternalData(ExternalDataDTO dto) throws Exception {
        if (dto.getPageSize() == null)
            dto.setPageSize(pageSize);
        if (dto.getFrequency() == null)
            dto.setFrequency(frequency);
        if (dto.getRequestUrl() == null)
            dto.setRequestUrl(defaultRequestUrl);
        if (dto.getPageNo() == null)
            dto.setPageNo(1);

        ModelInfo modelInfo = modelService.getModelInfoById(dto.getModelId());
        ModelTypeService modelTypeService = ModelType.valueOf(modelInfo.getSign().toLowerCase(Locale.ROOT)).getModelTypeService();
        List<SimulateDataInfo> infoList = modelTypeService.requestUrl(dto);
        return infoList;
    }


    /**
     * 任务初始化
     */
    private void init(ExternalDataDTO originDto) {
        if (originDto.getTaskId() == null) {
            throw new RuntimeException("taskId不能为null！");
        }
        pageSize = originDto.getPageSize();
        groupCount = originDto.getGroupCount();
        way = originDto.getWay();
        SimulateTaskInfo taskInfo = simulateTaskService.getById(originDto.getTaskId());
        String[] modelNameList = taskInfo.getModelName().split(",");
        String[] modelIdList = taskInfo.getModelId().split(",");
        List<StatusInfo> container = new ArrayList<>();
        for (int i = 0; i < modelNameList.length; i++) {
            String modelName = modelNameList[i];
            Integer modelId = Integer.parseInt(modelIdList[i]);
            StatusInfo statusInfo = new StatusInfo();
            statusInfo.setModelName(modelName);
            statusInfo.setModelId(modelId);
            statusInfo.setParentTaskId(originDto.getTaskId());
            ExternalDataDTO newExternalDto = new ExternalDataDTO();
            BeanUtils.copyProperties(originDto, newExternalDto);
            newExternalDto.setModelName(modelName);
            newExternalDto.setModelId(modelId);
            ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
            newExternalDto.setModelNameZh(modelInfo.getModelNameZh());
            statusInfo.setDto(newExternalDto);
            statusInfo.setGroupCount(originDto.getGroupCount());
            container.add(statusInfo);
        }
        statusInfoListMap.put(originDto.getTaskId(), container);
    }


    /**
     * 检测当前任务所对应的子模型任务是否全部完成
     */
    public static boolean checkSubTaskIsAchieve(Integer taskId) {
        List<StatusInfo> statusInfoList = statusInfoListMap.get(taskId);
        if (statusInfoList == null || statusInfoList.isEmpty()) {
            return true;
        }
        boolean key = true;
        for (StatusInfo statusInfo : statusInfoList) {
            key = key && statusInfo.getIsAchieve();
        }
        return key;
    }


    /**
     * 验证当前正在运行的线程是否已经超过阈值
     */
    private synchronized Boolean checkThreadCount() {
        if (currentThreadCount.get() < maxThreads) {
            currentThreadCount.addAndGet(1);
            return true;
        } else
            return false;
    }


    @Override
    public Map<String, Object> startTask(ExternalDataDTO dto) {
        Map<String, Object> result = new HashMap<>();
        if (dto.getTaskId() == null) {
            throw new RuntimeException("请传递taskId");
        }
        boolean isWait = simulateTaskService.queryIsWait(dto.getTaskId());
        if (!isWait) {
            result.put("status", "fail");
            result.put("message", "当前状态无法再次启动");
            return result;
        }
        if (cacheMap.containsKey(dto.getTaskId()))
            throw new RuntimeException("此任务已经拥有对应的线程处于启动或挂起状态;");
        if (checkThreadCount()) {
            if (dto.getPageSize() == null)
                dto.setPageSize(pageSize);
            if (dto.getFrequency() == null)
                dto.setFrequency(frequency);
            if (dto.getGroupCount() == null)
                dto.setGroupCount(groupCount);
            if (dto.getRequestUrl() == null)
                dto.setRequestUrl(defaultRequestUrl);
            if (dto.getPageNo() == null)
                dto.setPageNo(1);

            // 任务初始化 -> 初始化后的信息存储statusInfoListMap中
            init(dto);
            List<StatusInfo> statusInfoList = statusInfoListMap.get(dto.getTaskId());

            String threadName = "Thread-" + UUID.randomUUID().toString().replace("-", "");
            cacheMap.put(dto.getTaskId(), threadName);
            dtoMap.put(threadName, dto);
            isSuspendMap.put(threadName, false);
            updateModelAssessmentStatus(dto.getTaskId(), "RUN");

            Runnable task = () -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        int currentFrequency = frequency;
                        for (StatusInfo info : statusInfoList) {
                            ExternalDataDTO dto1 = info.getDto();
                            requestAndHandleExternalData(info, threadName, info.getParentTaskId(), info.getModelId()
                                    , dto1.getPageNo(), dto1.getPageSize(), dto1.getGroupCount());
                            currentFrequency = info.getDto().getFrequency();
                            info.getDto().setPageNo(info.getDto().getPageNo() + 1);
                        }
                        try {
                            Thread.sleep(60000 / currentFrequency);
                        } catch (Exception e) {
                            log.error("进程运行结束");
                        }
                        if (!threads.containsKey(threadName) && threads.get(threadName) == null)
                            return;
                        if (!threads.get(threadName).isInterrupted())
                            while (isSuspendMap.get(threadName)) {
                                Thread.sleep(1000);
                            }
                    } catch (InterruptedException e) {
                        simulateTaskService.changeTaskStatus(dto.getTaskId(), "ERROR");
                        this.updateModelAssessmentStatus(dto.getTaskId(), "ERROR");
                        removeFinishedTask(threadName, dto.getTaskId(), true);
                        if (currentThreadCount.get() > 0)
                            currentThreadCount.decrementAndGet();
                        throw new RuntimeException(e);
                    }
                }
            };
            Thread thread = new Thread(task, threadName);
            threads.put(threadName, thread);
            result.put("status", "ok");
            result.put("message", "线程启动成功");
            result.put("threadName", threadName);
            thread.start();
            simulateTaskService.changeTaskStatus(dto.getTaskId(), "RUN");
        } else {
            result.put("status", "fail");
            result.put("message", "当前正在运行的线程过多, 请稍后重试!");
        }
        return result;
    }


    @Override
    public Map<String, Object> stopTask(String threadName, Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        if (threadName == null && taskId == null) {
            throw new RuntimeException("请传递threadName或taskId");
        }
        if (threadName == null) {
            threadName = cacheMap.get(taskId);
            if (threadName == null) {
                throw new RuntimeException("当前任务没有对应的线程");
            }
        }
        Thread thread = threads.get(threadName);
        if (thread == null) {
            result.put("status", "ok");
            result.put("message", "当前线程尚未创建或已被终止");
        } else {
            thread.interrupt();
            if (currentThreadCount.get() > 0)
                currentThreadCount.decrementAndGet();
            result.put("status", "ok");
            result.put("message", "线程终止成功");
            ExternalDataDTO dto = dtoMap.get(threadName);
            simulateTaskService.changeTaskStatus(dto.getTaskId(), "FINISH");
            this.updateModelAssessmentStatus(dto.getTaskId(), "FINISH");
            removeFinishedTask(threadName, taskId, true);
        }
        return result;
    }


    @Override
    public synchronized Map<String, Object> suspendTask(String threadName, Integer taskId) throws InterruptedException {
        if (threadName == null && taskId == null) {
            throw new RuntimeException("请传递threadName或taskId");
        }
        if (threadName == null) {
            threadName = cacheMap.get(taskId);
        }
        Map<String, Object> result = new HashMap<>();
        if (threadName != null && isSuspendMap.containsKey(threadName)) {
            if (isSuspendMap.get(threadName)) {
                result.put("status", "fail");
                result.put("message", "线程已经处于挂起状态");
            } else {
                isSuspendMap.put(threadName, true);
                result.put("status", "ok");
                result.put("message", "线程挂起成功");
                ExternalDataDTO dto = dtoMap.get(threadName);
                simulateTaskService.changeTaskStatus(dto.getTaskId(), "PAUSE");
                this.updateModelAssessmentStatus(dto.getTaskId(), "PAUSE");
            }
        } else {
            result.put("status", "fail");
            result.put("message", "当前线程不存在");
        }
        return result;
    }


    @Override
    public synchronized Map<String, Object> resumeTask(String threadName, Integer taskId, Integer frequency, Integer pageSize) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (taskId == null) {
            throw new RuntimeException("请传递taskId字段");
        }
        if (threadName == null) {
            threadName = cacheMap.get(taskId);
        }
        if (threadName != null && isSuspendMap.containsKey(threadName)) {
            if (!isSuspendMap.get(threadName)) {
                result.put("status", "fail");
                result.put("message", "线程已经处于执行状态!");
                return result;
            }
            List<StatusInfo> statusInfoList = statusInfoListMap.get(taskId);
            // 重新计算分页内容，防止存在重复数据
            if (frequency != null) {
                for (StatusInfo info : statusInfoList) {
                    info.getDto().setFrequency(frequency);
                }
            }
            if (pageSize != null) {
                for (StatusInfo info : statusInfoList) {
                    ModelInfo modelInfo = modelService.getModelInfoById(info.getModelId());
                    Assert.notNull(modelInfo, "不存在id为" + info.getModelId() + "的模型信息");
                    ModelTypeService modelTypeService = ModelType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT)).getModelTypeService();

                    info.getDto().setPageSize(pageSize);
                    int newPageNo = (info.getAchieveCount() / pageSize) + 1;   // 新页码
                    info.getDto().setPageNo(newPageNo);
                    // 独立请求, 将偏移量存入数据库
                    int offset = info.getAchieveCount() - (pageSize * (newPageNo - 1));
                    List<Object> responseJson = modelTypeService.requestUrl(info.getDto());
                    modelTypeService.handleExternalData(responseJson, threadName, offset, info, taskId, info.modelId);
                    info.getDto().setPageNo(newPageNo + 1);
                }
            }
            isSuspendMap.put(threadName, false);
            result.put("status", "ok");
            result.put("message", "线程恢复成功");
            ExternalDataDTO dto = dtoMap.get(threadName);
            simulateTaskService.changeTaskStatus(dto.getTaskId(), "RUN");
            this.updateModelAssessmentStatus(dto.getTaskId(), "RUN");
        } else {
            result.put("status", "fail");
            result.put("message", "当前线程不存在");
        }
        return result;
    }


    /**
     * @Description TODO
     * @Author getao
     * @Date 19:56 2025/3/21
     * @Param [info, threadName, taskId, modelId, pageNo, pageSize, groupCount]
     * @Param [每个模型的引接状态信息, threadName, taskId, modelId, pageNo, pageSize, groupCount]
     * @return void
     */
    private synchronized void requestAndHandleExternalData(StatusInfo info, String threadName
            , Integer taskId, Integer modelId, Integer pageNo, Integer pageSize, Integer groupCount) throws InterruptedException {
        Runnable subTask = () -> {
            try {
                ExternalDataDTO dto = new ExternalDataDTO();
                BeanUtils.copyProperties(info.getDto(), dto);
                dto.setPageSize(pageSize);
                dto.setPageNo(pageNo);
                dto.setGroupCount(groupCount);
                // 请求外部数据
                SimulateTaskInfo taskInfo = taskDao.selectById(taskId);
                ModelTypeService modelTypeService = commonService.getModelTypeService(modelId);
                List<Object> responseJson = modelTypeService.requestUrl(dto);

                if ((responseJson == null || responseJson.size() == 0) && !taskInfo.getTaskType().equalsIgnoreCase("CJSLBS")) {            // 判断是否还有新数据 若无新数据则自动终止线程
                    if (threads.containsKey(threadName) && !info.getIsAchieve()) {
                        info.setIsAchieve(true);
                        assessmentService.updateStatus(dto.getTaskId(), info.getModelId(), "FINISH");
                        info.setMAIFlag(true);
                    }
                    boolean isAchieve = checkSubTaskIsAchieve(info.getParentTaskId());
                    if (isAchieve) {
                        if (threads.containsKey(threadName)) {
                            threads.get(threadName).interrupt();
                            if (currentThreadCount.get() > 0)
                                currentThreadCount.decrementAndGet();
                            log.info("已无新数据, 线程已自动结束!");
                            simulateTaskService.changeTaskStatus(info.getParentTaskId(), "FINISH");
                            this.updateModelAssessmentStatus(dto.getTaskId(), "FINISH");
                            removeFinishedTask(threadName, info.parentTaskId, false);
                        }
                    }
                } else if (responseJson != null || responseJson.size() != 0) {
                    // 存储外部数据
                    if (!info.isAchieve) {
                        modelTypeService.handleExternalData(responseJson, threadName, 0, info, taskId, modelId);
                    }
                }
            } catch (Exception e) {
                simulateTaskService.changeTaskStatus(info.getParentTaskId(), "ERROR");
                this.updateModelAssessmentStatus(info.getParentTaskId(), "ERROR");
                if (currentThreadCount.get() > 0)
                    currentThreadCount.decrementAndGet();
                removeFinishedTask(threadName, info.getParentTaskId(), true);
                log.error("内部错误！");
                throw new RuntimeException(e);
            }
        };
        Thread thread = new Thread(subTask, UUID.randomUUID().toString());
        thread.start();
    }


    /**
     * 删除已经执行完毕的任务信息
     * isForceDelete : 是否强制删除
     */
    public void removeFinishedTask(String threadName, Integer taskId, boolean isForceDelete) {
        boolean flag = true;
        List<StatusInfo> statusInfoList = statusInfoListMap.get(taskId);
        if (statusInfoList == null) return;
        for (StatusInfo info : statusInfoList) {
            flag = flag && info.getMAIFlag();
        }
        if (isForceDelete || flag) {
            dtoMap.remove(threadName);
            threads.remove(threadName);
            isSuspendMap.remove(threadName);
            cacheMap.remove(taskId);
            statusInfoListMap.remove(taskId);
        }
    }


    /**
     * @author: getao
     * @Date: 2024/10/21 15:48
     * @Description: 更新模型评估记录状态
     */
    public void updateModelAssessmentStatus(int taskId, String status) {
        List<StatusInfo> statusInfoList = statusInfoListMap.get(taskId);
        if (statusInfoList == null)
            return;
        for (StatusInfo info : statusInfoList) {
            assessmentService.updateStatus(taskId, info.getModelId(), status);
            info.setMAIFlag(true);
        }
    }


    /**
     * @author: getao
     * @Date: 2024/12/19 9:04
     * @Description: 引接模型真实数据
     */
    @Override
    public JSONObject getModelRealData(int taskId, int modelId) {
        ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        ModelTypeService modelTypeService = ModelType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT)).getModelTypeService();
        JSONObject realData = modelTypeService.getSimulateRealData(taskId, modelId);
        return realData;
    }


    /**
     * @author: getao
     * @Date: 2024/12/19 15:05
     * @Description: 采集仿真数据
     */
    @Override
    public JSONObject pullSimulateData(int taskId, int modelId) {
        ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        ModelTypeService modelTypeService = ModelType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT)).getModelTypeService();
        JSONObject realData = modelTypeService.pullSimulateData(taskId, modelId);
        return realData;
    }


    /**
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     * @Description 根据仿真任务id和模型id获取模型实际数据
     * @Author getao
     * @Date 9:53 2025/3/17
     * @Param [taskId, modelId]
     */
    @Override
    public JSONObject listModelRealData(int taskId, int modelId) {
        ModelTypeService modelTypeService = commonService.getModelTypeService(modelId);
        Assert.notNull(modelTypeService, "寻址模型id" + modelId + "的服务Bean出错或Bean为空");
        JSONObject list = modelTypeService.listModelRealData(taskId, modelId);
        return list;
    }


    /**
     * @return com.alibaba.fastjson.JSONObject
     * @Description 根据仿真任务id和模型id获取模型输出/仿真数据
     * @Author getao
     * @Date 12:52 2025/3/17
     * @Param [taskId, modelId]
     */
    @Override
    public JSONObject listModelOutputData(int taskId, int modelId) {
        ModelTypeService modelTypeService = commonService.getModelTypeService(modelId);
        Assert.notNull(modelTypeService, "寻址模型id" + modelId + "的服务Bean出错或Bean为空");
        JSONObject list = modelTypeService.listModelOutputData(taskId, modelId);
        return list;
    }


    /**
     * @return com.alibaba.fastjson.JSONObject
     * @Description 获取模型运行数据引接进度
     * @Author getao
     * @Date 15:58 2025/3/19
     * @Param [taskId]
     */
    @Override
    public List<JSONObject> getExternalDataStatic(int taskId) {
        List<JSONObject> result = new ArrayList<>();
        QueryWrapper<ModelRunlDataInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        List<ModelRunlDataInfo> runlDataInfos = this.runDataService.list(wrapper);
        if (runlDataInfos == null || runlDataInfos.size() == 0)
            return new ArrayList<>();

        Map<Integer, List<ModelRunlDataInfo>> listMap = runlDataInfos.stream().collect(Collectors.groupingBy(ModelRunlDataInfo::getModelId));
        Set<Map.Entry<Integer, List<ModelRunlDataInfo>>> modelDatas = listMap.entrySet();
        for (Map.Entry<Integer, List<ModelRunlDataInfo>> modelData : modelDatas) {
            List<ModelRunlDataInfo> dataInfos = modelData.getValue();
            long existCount = dataInfos.stream().filter(e -> e.getExistInput()).count();
            JSONObject accessProgress = new JSONObject();
            Integer modelId = modelData.getKey();
            ModelInfo modelInfo = this.modelService.getById(modelId);
            String modelName = modelInfo.getModelName();
            long total = 0;
            if (way.equalsIgnoreCase("KEEP")) {
                total = pageSize * groupCount;
            } else {
                total = MathUtils.sumOfGroups(groupCount, pageSize, pageSize);
            }
            long done = dataInfos.size();
            double rate = MathUtils.halfUpScale2(done / (double) total);
            accessProgress.put("modelName", modelName);
            accessProgress.put("total", total);
            accessProgress.put("done", done);
            accessProgress.put("existCount", existCount);
            accessProgress.put("notExistCount", total - existCount);
            accessProgress.put("rate", rate * 100);
            result.add(accessProgress);
        }
        return result;
    }
}
