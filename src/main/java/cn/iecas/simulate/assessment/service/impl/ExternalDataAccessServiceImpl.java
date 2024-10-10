package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.service.ExternalDataAccessService;
import cn.iecas.simulate.assessment.service.SimulateDataService;
import cn.iecas.simulate.assessment.service.SimulateTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
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

    @Autowired
    private SimulateDataService simulateDataService;

    @Autowired
    private SimulateTaskService simulateTaskService;

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

    private static final ConcurrentHashMap<String, Integer> achieveCountMap = new ConcurrentHashMap<>();

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
        if (checkThreadCount()){
            if (dto.getPageSize() == null)
                dto.setPageSize(pageSize);
            if (dto.getFrequency() == null)
                dto.setFrequency(frequency);
            if (dto.getRequestUrl() == null)
                dto.setRequestUrl(defaultRequestUrl);
            if (dto.getPageNo() == null)
                dto.setPageNo(1);
            String threadName = "EThread-" + UUID.randomUUID().toString().replace("-", "");
            dtoMap.put(threadName, dto);
            isSuspendMap.put(threadName, false);
            achieveCountMap.put(threadName, 0);
            Runnable task = () -> {
                while (!Thread.currentThread().isInterrupted()){
                    try {
                        requestAndHandleExternalData(dto, threadName);
                        synchronized (dto){
                            dto.setPageNo(dto.getPageNo() + 1);
                        }
                        Thread.sleep(60000 / dto.getFrequency());
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
    public Map<String, Object> stopTask(String threadName) {
        Map<String, Object> result = new HashMap<>();
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
            removeFinishedTask(threadName);
        }
        return result;
    }


    @Override
    public synchronized Map<String, Object> suspendTask(String threadName) throws InterruptedException {
        Map<String, Object> result = new HashMap<>();
        if (isSuspendMap.containsKey(threadName)) {
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
    public synchronized Map<String, Object> resumeTask(String threadName, Integer frequency, Integer pageSize) throws Exception {
        Map<String, Object> result = new HashMap<>();
            if (isSuspendMap.containsKey(threadName)) {
                if (!isSuspendMap.get(threadName)){
                    result.put("status", "fail");
                    result.put("message", "线程已经处于执行状态!");
                    return result;
                }
                // 重新计算分页内容，防止存在重复数据
                if (frequency != null)
                    dtoMap.get(threadName).setFrequency(frequency);
                if (pageSize != null) {
                    dtoMap.get(threadName).setPageSize(pageSize);
                    int newPageNo = (achieveCountMap.get(threadName) / pageSize) + 1;   // 新页码
                    dtoMap.get(threadName).setPageNo(newPageNo);
                    // 独立请求, 将偏移量存入数据库
                    int offset = achieveCountMap.get(threadName) - (pageSize * (newPageNo - 1));
                    String responseJson = requestUrl(dtoMap.get(threadName));
                    handleExternalData(responseJson, threadName, offset + 1);
                    dtoMap.get(threadName).setPageNo(newPageNo + 1);
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
    private void requestAndHandleExternalData(ExternalDataDTO dto, String threadName) throws InterruptedException {
        Runnable subTask = () -> {
            try {
                String responseJson = requestUrl(dto);      // 请求外部数据
                if (responseJson.length() == 0 || responseJson.equals("[]")){            // 判断是否还有新数据 若无新数据则自动终止线程
                    if (threads.containsKey(threadName)) {
                        threads.get(threadName).interrupt();
                        currentThreadCount.decrementAndGet();
                        log.info("已无新数据, 线程已自动结束!");
                        simulateTaskService.changeTaskStatus(dto.getTaskId(), "FINISH");
                        removeFinishedTask(threadName);
                    }
                }
                else
                    handleExternalData(responseJson, threadName, 0);           // 存储外部数据
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
    private void handleExternalData(String externalDataJson, String threadName, Integer offset){
        // 根据第三方接口主要修改下面这行代码
        List<SimulateDataInfo> infoList = JSON.parseArray(externalDataJson, SimulateDataInfo.class);

        List<SimulateDataInfo> newInfoList = new ArrayList<>();
        if (offset == 0) {
            for (int i = offset; i < infoList.size(); i++) {
                infoList.get(i).setId(null);
            }
            simulateDataService.insertBatch(infoList);
            achieveCountMap.put(threadName, achieveCountMap.get(threadName) + infoList.size());     // 保存已经完成的数据的数量
        }
        else {
            for (int i = offset; i < infoList.size(); i++) {
                infoList.get(i).setId(null);
                newInfoList.add(infoList.get(i));
            }
            simulateDataService.insertBatch(newInfoList);
            achieveCountMap.put(threadName, achieveCountMap.get(threadName) + newInfoList.size());     // 保存已经完成的数据的数量
        }

    }


    /**
     * 调用第三方接口获取信息
     */
    private String requestUrl(ExternalDataDTO params) throws Exception {
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
    private void removeFinishedTask(String threadName){
        dtoMap.remove(threadName);
        threads.remove(threadName);
        isSuspendMap.remove(threadName);
        achieveCountMap.remove(threadName);
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
