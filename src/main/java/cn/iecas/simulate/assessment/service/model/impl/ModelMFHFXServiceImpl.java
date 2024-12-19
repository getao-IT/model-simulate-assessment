package cn.iecas.simulate.assessment.service.model.impl;

import cn.aircas.utils.date.DateUtils;
import cn.iecas.simulate.assessment.dao.AssessmentStatisticDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.domain.ModelIndexInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import cn.iecas.simulate.assessment.service.*;
import cn.iecas.simulate.assessment.service.impl.ExternalDataAccessServiceImpl;
import cn.iecas.simulate.assessment.service.impl.RestTemplateApi;
import cn.iecas.simulate.assessment.service.model.ModelTypeService;
import cn.iecas.simulate.assessment.service.model.SimulateDataService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * @auther getao
 * @date 2024/10/30 17:33
 * @description 府会分析模型处理服务类
 */
@Slf4j
@Service("MFHFX-SERVICE")
public class ModelMFHFXServiceImpl implements ModelTypeService<SimulateDataInfo> {

    @Autowired
    private SimulateTaskService simulateTaskService;

    @Autowired
    private ModelAssessmentService assessmentService;

    @Autowired
    private RestTemplateApi templateApi;

    @Autowired
    private SimulateTaskDao taskDao;

    @Autowired
    private AssessmentStatisticDao statisticDao;

    @Autowired
    private ModelService modelService;

    @Autowired
    @Qualifier(value = "MFHFX-DATASERVICE")
    private SimulateDataService simulateDataService;

    @Autowired
    private ModelCommonServiceImpl modelCommonService;


    /**
     * @Description 获取模型引接数据
     * @auther getao
     * @Date 2024/10/30 18:44
     * @Param [object]
     * @Return java.lang.String
     */
    @Override
    public List<SimulateDataInfo> requestUrl(Object object) throws Exception {
        ExternalDataDTO params = (ExternalDataDTO) object;
        params.setPageNum(params.getPageNo());
        SimulateTaskInfoDto taskInfoDto = new SimulateTaskInfoDto();
        BeanUtils.copyProperties(params, taskInfoDto);

        JSONObject simulateData = this.templateApi.getSimulateData(taskInfoDto);
        List<SimulateDataInfo> dataInfos = simulateData.getJSONObject("data").getJSONArray("dataList")
                .toJavaList(SimulateDataInfo.class);
        log.info("本次引接的数据 第 {} 页，每页 {} 条数，实际 {} 条 ......", taskInfoDto.getPageNo(), taskInfoDto.getPageSize(), dataInfos.size());
        return dataInfos;
    }

    /**
     * @Description 处理模型引接数据
     * @auther getao
     * @Date 2024/10/30 18:45
     * @Param [externalDataJson, threadName, offset, info, taskId, modelId]
     * @Return void
     */
    @Override
    public void handleExternalData(List<SimulateDataInfo> externalDataJson, String threadName, Integer offset, ExternalDataAccessServiceImpl.StatusInfo info, Integer taskId, Integer modelId) {
        // 根据第三方接口主要修改下面这行代码
        List<SimulateDataInfo> infoList = externalDataJson;
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
                if (infoList.get(i).getTerritory().contains("生活丑闻")) {
                    infoList.get(i).setType("元首政要丑闻");
                } else {
                    infoList.get(i).setType("议员政策法案");
                }
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

        this.modelCommonService.updateSimulsteTaskInfo(taskId, newDataCount);
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


    /**
     *  @author: getao
     *  @Date: 2024/12/18 17:46
     *  @Description: 获取该模型实际数据
     */
    @Override
    public JSONObject getSimulateRealData(int taskId, int modelId) {
        JSONObject result = new JSONObject();
        TbModelInfo modelInfo = modelService.getModelInfoById(modelId);
        result.put("source", modelInfo.getModelName());

        JSONObject simulateRealData = this.templateApi.getSimulateRealData(modelInfo);
        result.put("data", simulateRealData.getJSONObject("data").get("dataList"));

        long dataSize = simulateRealData.toJSONString().length();
        result.put("size", dataSize + "字节");
        return result;
    }


    /**
     *  @author: getao
     *  @Date: 2024/12/19 15:06
     *  @Description: 采集仿真数据
     */
    @Override
    public JSONObject pullSimulateData(int taskId, int modelId) {
        JSONObject result = new JSONObject();
        TbModelInfo modelInfo = modelService.getModelInfoById(modelId);
        result.put("source", "平行仿真平台-"+modelInfo.getModelName());

        JSONObject simulateRealData = this.templateApi.pullSimulateData(modelInfo);
        result.put("data", simulateRealData.getJSONObject("data").get("dataList"));

        long dataSize = simulateRealData.toJSONString().length();
        result.put("size", dataSize + "字节");
        return result;
    }
}
