package cn.iecas.simulate.assessment.service.impl;


import cn.aircas.utils.date.DateUtils;
import cn.iecas.simulate.assessment.dao.AssessmentStatisticDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import cn.iecas.simulate.assessment.service.ExternalDataAccessService;
import cn.iecas.simulate.assessment.service.SimulateDataService;
import cn.iecas.simulate.assessment.service.SimulateTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;



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
    private static class StatusInfo{

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
         * 对应查询条件
         */
        public ExternalDataDTO dto;
    }

    @Autowired
    private SimulateDataService simulateDataService;

    @Autowired
    private SimulateTaskService simulateTaskService;

    @Autowired
    private RestTemplateApi templateApi;

    @Autowired
    private SimulateTaskDao taskDao;

    @Autowired
    private AssessmentStatisticDao statisticDao;

    @Value("${external-data-access.frequency}")
    private Integer frequency;

    @Value("${external-data-access.pageSize}")
    private Integer pageSize;

    @Value("${external-data-access.default-request-url}")
    private String defaultRequestUrl;

    @Value("${external-data-access.thread-setting.max}")
    private int maxThreads;

    public static final ConcurrentHashMap<String, Thread> threads = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, ExternalDataDTO> dtoMap = new ConcurrentHashMap<>();

    public static final AtomicInteger currentThreadCount = new AtomicInteger(0);

    private static final ConcurrentHashMap<String, Boolean> isSuspendMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Integer, String> cacheMap = new ConcurrentHashMap<>();

    /**
     * 缓存
     */
    private static final ConcurrentHashMap<Integer, List<StatusInfo>> statusInfoListMap = new ConcurrentHashMap<>();

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

        String responseJson = requestUrl(dto);
        List<SimulateDataInfo> infoList = JSON.parseArray(responseJson, SimulateDataInfo.class);
        return infoList;
    }


    /**
     * 任务初始化
     */
    private void init(ExternalDataDTO originDto){
        if (originDto.getTaskId() == null){
            throw new RuntimeException("taskId不能为null！");
        }
        SimulateTaskInfo taskInfo = simulateTaskService.getById(originDto.getTaskId());
        String[] modelNameList = taskInfo.getModelName().split(",");
        String[] modelIdList = taskInfo.getModelId().split(",");
        List<StatusInfo> container = new ArrayList<>();
        for (int i = 0; i < modelNameList.length; i++){
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
            statusInfo.setDto(newExternalDto);
            container.add(statusInfo);
        }
        statusInfoListMap.put(originDto.getTaskId(), container);
    }


    /**
     * 检测当前任务所对应的子模型任务是否全部完成
     */
    private boolean checkSubTaskIsAchieve(Integer taskId){
        List<StatusInfo> statusInfoList = statusInfoListMap.get(taskId);
        if (statusInfoList == null || statusInfoList.isEmpty()){
            return true;
        }
        boolean key = true;
        for (StatusInfo statusInfo : statusInfoList){
            key = key && statusInfo.getIsAchieve();
        }
        return key;
    }


    /**
     * 验证当前正在运行的线程是否已经超过阈值
     */
    private synchronized Boolean checkThreadCount(){
        if (currentThreadCount.get() < maxThreads){
            currentThreadCount.addAndGet(1);
            return true;
        }
        else
            return false;
    }


    @Override
    public Map<String, Object> startTask(ExternalDataDTO dto) {
        Map<String, Object> result = new HashMap<>();
        if (dto.getTaskId() == null){
            throw new RuntimeException("请传递taskId");
        }
        boolean isWait = simulateTaskService.queryIsWait(dto.getTaskId());
        if (!isWait){
            result.put("status", "fail");
            result.put("message", "当前状态无法再次启动");
            return result;
        }
        if (cacheMap.containsKey(dto.getTaskId()))
            throw new RuntimeException("此任务已经拥有对应的线程处于启动或挂起状态;");
        if (checkThreadCount()){
            if (dto.getPageSize() == null)
                dto.setPageSize(pageSize);
            if (dto.getFrequency() == null)
                dto.setFrequency(frequency);
            if (dto.getRequestUrl() == null)
                dto.setRequestUrl(defaultRequestUrl);
            if (dto.getPageNo() == null)
                dto.setPageNo(1);
            String threadName = "Thread-" + UUID.randomUUID().toString().replace("-", "");

            // 任务初始化 -> 初始化后的信息存储statusInfoListMap中
            init(dto);
            List<StatusInfo> statusInfoList = statusInfoListMap.get(dto.getTaskId());

            cacheMap.put(dto.getTaskId(), threadName);
            dtoMap.put(threadName, dto);
            isSuspendMap.put(threadName, false);

            Runnable task = () -> {
                while (!Thread.currentThread().isInterrupted()){
                    try {
                        int currentFrequency = frequency;
                        for (StatusInfo info : statusInfoList){
                            requestAndHandleExternalData(info, threadName, info.getParentTaskId(), info.getModelId()
                                    , info.getDto().getPageNo(), info.getDto().getPageSize());
                            currentFrequency = info.getDto().getFrequency();
                            info.getDto().setPageNo(info.getDto().getPageNo() + 1);
                        }
                        try{
                            Thread.sleep(60000 / currentFrequency);
                        } catch (Exception e){
                            log.info("进程运行结束");
                        }
                        if (!threads.containsKey(threadName) && threads.get(threadName) == null)
                            return;
                        if (!threads.get(threadName).isInterrupted())
                            while (isSuspendMap.get(threadName)){
                                Thread.sleep(1000);
                            }
                    } catch (InterruptedException e) {
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
        }
        else {
            result.put("status", "fail");
            result.put("message", "当前正在运行的线程过多, 请稍后重试!");
        }
        return result;
    }


    @Override
    public Map<String, Object> stopTask(String threadName, Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        if (threadName == null && taskId == null){
            throw new RuntimeException("请传递threadName或taskId");
        }
        if (threadName == null){
            threadName = cacheMap.get(taskId);
            if (threadName == null){
                throw new RuntimeException("当前任务没有对应的线程");
            }
        }
        Thread thread = threads.get(threadName);
        if (thread == null){
            result.put("status", "ok");
            result.put("message", "当前线程尚未创建或已被终止");
        }
        else {
            thread.interrupt();
            currentThreadCount.decrementAndGet();
            result.put("status", "ok");
            result.put("message", "线程终止成功");
            ExternalDataDTO dto = dtoMap.get(threadName);
            simulateTaskService.changeTaskStatus(dto.getTaskId(), "FINISH");
            removeFinishedTask(threadName, taskId);
        }
        return result;
    }


    @Override
    public synchronized Map<String, Object> suspendTask(String threadName, Integer taskId) throws InterruptedException {
        if (threadName == null && taskId == null){
            throw new RuntimeException("请传递threadName或taskId");
        }
        if (threadName == null){
            threadName = cacheMap.get(taskId);
        }
        Map<String, Object> result = new HashMap<>();
        if (threadName != null && isSuspendMap.containsKey(threadName)) {
            if (isSuspendMap.get(threadName)){
                result.put("status", "fail");
                result.put("message", "线程已经处于挂起状态");
            } else {
                isSuspendMap.put(threadName, true);
                result.put("status", "ok");
                result.put("message", "线程挂起成功");
                ExternalDataDTO dto = dtoMap.get(threadName);
                simulateTaskService.changeTaskStatus(dto.getTaskId(), "PAUSE");
            }
        }
        else {
            result.put("status", "fail");
            result.put("message", "当前线程不存在");
        }
        return result;
    }


    @Override
    public synchronized Map<String, Object> resumeTask(String threadName, Integer taskId, Integer frequency, Integer pageSize) throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (taskId == null){
            throw new RuntimeException("请传递taskId字段");
        }
        if (threadName == null){
            threadName = cacheMap.get(taskId);
        }
        if (threadName != null && isSuspendMap.containsKey(threadName)) {
            if (!isSuspendMap.get(threadName)){
                result.put("status", "fail");
                result.put("message", "线程已经处于执行状态!");
                return result;
            }
            List<StatusInfo> statusInfoList = statusInfoListMap.get(taskId);
            // 重新计算分页内容，防止存在重复数据
            if (frequency != null){
                for (StatusInfo info : statusInfoList){
                    info.getDto().setFrequency(frequency);
                }
            }
            if (pageSize != null) {
                for (StatusInfo info : statusInfoList) {
                    info.getDto().setPageSize(pageSize);
                    int newPageNo = (info.getAchieveCount() / pageSize) + 1;   // 新页码
                    info.getDto().setPageNo(newPageNo);
                    // 独立请求, 将偏移量存入数据库
                    int offset = info.getAchieveCount() - (pageSize * (newPageNo - 1));
                    String responseJson = requestUrl(info.getDto());
                    handleExternalData(responseJson, threadName, offset, info, taskId, info.modelId);
                    info.getDto().setPageNo(newPageNo + 1);
                }
            }
            isSuspendMap.put(threadName, false);
            result.put("status", "ok");
            result.put("message", "线程恢复成功");
            ExternalDataDTO dto = dtoMap.get(threadName);
            simulateTaskService.changeTaskStatus(dto.getTaskId(), "RUN");
        } else {
            result.put("status", "fail");
            result.put("message", "当前线程不存在");
        }
        return result;
    }


    /**
     * 请求外部数据并处理
     */
    private synchronized void requestAndHandleExternalData(StatusInfo info, String threadName
            , Integer taskId, Integer modelId, Integer pageNo, Integer pageSize) throws InterruptedException {
        Runnable subTask = () -> {
            try {
                ExternalDataDTO dto = new ExternalDataDTO();
                BeanUtils.copyProperties(info.getDto(), dto);
                dto.setPageSize(pageSize);
                dto.setPageNo(pageNo);
                String responseJson = requestUrl(dto);      // 请求外部数据
                if (responseJson.length() == 0 || responseJson.equals("[]")){            // 判断是否还有新数据 若无新数据则自动终止线程
                    if (threads.containsKey(threadName) && !info.getIsAchieve()) {
                        info.setIsAchieve(true);
                    }
                    boolean isAchieve = checkSubTaskIsAchieve(info.getParentTaskId());
                    if (isAchieve){
                        if (threads.containsKey(threadName)) {
                            threads.get(threadName).interrupt();
                            removeFinishedTask(threadName, info.parentTaskId);
                            currentThreadCount.decrementAndGet();
                            log.info("已无新数据, 线程已自动结束!");
                            simulateTaskService.changeTaskStatus(info.getParentTaskId(), "FINISH");
                        }
                    }
                }
                else {
                    handleExternalData(responseJson, threadName, 0, info, taskId, modelId);           // 存储外部数据
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        Thread thread = new Thread(subTask, UUID.randomUUID().toString());
        thread.start();
    }


    /**
     * 解析外部数据并存入数据库中
     * 注: 此部分内容可能需要根据实际的第三方接口的内容进行简单的调整
     */
    private void handleExternalData(String externalDataJson, String threadName, Integer offset, StatusInfo info
            ,Integer taskId, Integer modelId){
        // 根据第三方接口主要修改下面这行代码
        List<SimulateDataInfo> infoList = JSON.parseArray(externalDataJson, SimulateDataInfo.class);
        List<SimulateDataInfo> newInfoList = new ArrayList<>();

        // 本次新增数据量
        int newDataCount = 0;

        if (offset == 0) {
            for (int i = offset; i < infoList.size(); i++) {
                infoList.get(i).setId(null);
                // 更新模型id和任务id和引入时间
                infoList.get(i).setModelId(modelId);
                infoList.get(i).setTaskId(taskId);
                infoList.get(i).setImportTime(DateUtils.nowDate());
            }
            simulateDataService.insertBatch(infoList);
            info.setAchieveCount(info.getAchieveCount() + infoList.size());
            newDataCount = infoList.size();
        }
        else {
            for (int i = offset; i < infoList.size(); i++) {
                infoList.get(i).setId(null);
                infoList.get(i).setModelId(modelId);
                infoList.get(i).setTaskId(taskId);
                infoList.get(i).setImportTime(DateUtils.nowDate());
                newInfoList.add(infoList.get(i));
            }
            simulateDataService.insertBatch(newInfoList);
            info.setAchieveCount(info.getAchieveCount() + newInfoList.size());
            newDataCount = newInfoList.size();
        }

        // 更新仿真任务数据仿真消耗时间、总条数、平均引接数
        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        QueryWrapper<AssessmentStatisticInfo> statisticInfoWra = new QueryWrapper<>();
        statisticInfoWra.eq("task_id", taskId);
        AssessmentStatisticInfo statisticInfo = this.statisticDao.selectOne(statisticInfoWra);
        Date createTime = taskInfo.getCreateTime();
        long consumTime = System.currentTimeMillis() - createTime.getTime();
        String consumTimeStr = cn.iecas.simulate.assessment.util.DateUtils.millisToTime(consumTime);
        statisticInfo.setTimeConsuming(consumTimeStr);
        int dataCount = statisticInfo.getSimulateDataCount() + newDataCount;
        statisticInfo.setSimulateDataCount(dataCount);
        double avgImportNum = new BigDecimal(dataCount / (consumTime / 1000.0))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        statisticInfo.setAvgImportNum(avgImportNum);
        statisticInfo.setCallCount(statisticInfo.getCallCount()+1);
        double importFrequency = new BigDecimal(statisticInfo.getCallCount() / (consumTime / (1000.0 * 60)))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        statisticInfo.setImportFrequency(importFrequency);
        this.statisticDao.updateById(statisticInfo);
    }


    /**
     * 调用第三方接口获取信息
     */
    private String requestUrl(ExternalDataDTO params) throws Exception {

        params.setPageNum(params.getPageNo());
        SimulateTaskInfoDto taskInfoDto = new SimulateTaskInfoDto();
        BeanUtils.copyProperties(params, taskInfoDto);

//        JSONObject simulateData = this.templateApi.getSimulateData(taskInfoDto);
//        List<SimulateDataInfo> dataInfos = simulateData.getJSONObject("data").getJSONArray("dataList")
//                .toJavaList(SimulateDataInfo.class);
//
//        return JSON.toJSONString(dataInfos);

//        生产环境下把下面代码注释掉 把上面注释掉的打开
        String urlWithParams = params.getRequestUrl() + "?" + buildQueryString(params);
        URL url = new URL(urlWithParams);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        // 获取响应码
        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK){
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = bufferedReader.readLine()) != null){
                response.append(inputLine);
            }
            bufferedReader.close();
            JSONObject jsonObject = JSON.parseObject(response.toString());

            // TODO 此部分内容可能需要根据外部接口的实际返回内容进行修改
            return JSON.parseObject(jsonObject.getString("data")).getString("result");
        }
        else {
            simulateTaskService.changeTaskStatus(params.getTaskId(), "ERROR");
            throw new RuntimeException("调用第三方接口异常");
        }
    }


    /**
     * 删除已经执行完毕的任务信息
     */
    private void removeFinishedTask(String threadName, Integer taskId){
        dtoMap.remove(threadName);
        threads.remove(threadName);
        isSuspendMap.remove(threadName);
        cacheMap.remove(taskId);
        statusInfoListMap.remove(taskId);
    }


    /**
     * 过反射将 DTO 对象转换为查询字符串
     */
    private static String buildQueryString(Object dto) throws IllegalAccessException, UnsupportedEncodingException {
        StringBuilder queryString = new StringBuilder();
        Field[] fields = dto.getClass().getDeclaredFields(); // 获取所有字段
        boolean firstParam = true;
        List<String> exclusionName = new ArrayList<>(Arrays.asList("requestUrl", "frequency"));
        for (Field field : fields) {
            field.setAccessible(true); // 设置为可访问

            if (exclusionName.contains(field.getName()))
                continue;

            if (field.get(dto) != null) { // 检查字段是否为空
                if (!firstParam) {
                    queryString.append("&");
                } else {
                    firstParam = false;
                }
                // 对参数进行 URL 编码，避免特殊字符破坏 URL 结构
                queryString.append(URLEncoder.encode(field.getName(), StandardCharsets.UTF_8.toString()));
                queryString.append("=");
                queryString.append(URLEncoder.encode(field.get(dto).toString(), StandardCharsets.UTF_8.toString()));
            }
        }
        return queryString.toString();
    }
}
