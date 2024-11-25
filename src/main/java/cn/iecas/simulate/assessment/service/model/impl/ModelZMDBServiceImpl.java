package cn.iecas.simulate.assessment.service.model.impl;

import cn.aircas.utils.date.DateUtils;
import cn.iecas.simulate.assessment.dao.model.ZbCompareDao;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.entity.model.domain.IndexIndicatorTaskInfo;
import cn.iecas.simulate.assessment.entity.model.domain.ZbCompareInfo;
import cn.iecas.simulate.assessment.service.impl.ExternalDataAccessServiceImpl;
import cn.iecas.simulate.assessment.service.impl.RestTemplateApi;
import cn.iecas.simulate.assessment.service.model.ModelTypeService;
import cn.iecas.simulate.assessment.service.model.SimulateDataService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * @auther getao
 * @Date 2024/10/31 9:31
 * @Description 中美对比模型服务实现类
 */
@Slf4j
@Service("ZMDB-SERVICE")
public class ModelZMDBServiceImpl implements ModelTypeService<IndexIndicatorTaskInfo> {

    @Value("${assessment.zmdb.modeltype}")
    private String modelType;

    @Autowired
    private RestTemplateApi templateApi;

    @Autowired
    @Qualifier(value = "ZMDB-TASK-DATASERVICE")
    private SimulateDataService simulateDataService;

    @Autowired
    private ModelCommonServiceImpl modelCommonService;

    @Autowired
    private ZbCompareDao compareDao;

    @Autowired
    @Qualifier(value = "ZMDB-COMPARE-DATASERVICE")
    private DataZbCompareServiceImpl compareService;


    /**
     * @Description 获取模型引接数据
     * @auther getao
     * @Date 2024/10/30 18:44
     * @Param [object]
     * @Return java.lang.String
     */
    @Override
    public List<IndexIndicatorTaskInfo> requestUrl(Object object) {
        ExternalDataDTO params = (ExternalDataDTO) object;
        params.setModelType(modelType);
        JSONObject simulateData = this.templateApi.getAllIndexIndicatorTask(params);
        List<IndexIndicatorTaskInfo> indicatorTasks = simulateData.getJSONObject("data").getJSONArray("dataList")
                .toJavaList(IndexIndicatorTaskInfo.class);
        return indicatorTasks;
    }


    /**
     * @Description 处理模型引接数据
     * @auther getao
     * @Date 2024/10/30 18:45
     * @Param [externalDataJson, threadName, offset, info, taskId, modelId]
     * @Return void
     */
    @Override
    @Transactional
    public void handleExternalData(List<IndexIndicatorTaskInfo> externalData, String threadName, Integer offset,
                                   ExternalDataAccessServiceImpl.StatusInfo info, Integer taskId, Integer modelId) {
        List<ZbCompareInfo> compareInfos = new ArrayList<>();
        for (int i = offset; i < externalData.size(); i++) {
            IndexIndicatorTaskInfo taskInfo = externalData.get(i);
            taskInfo.setModelId(modelId);
            taskInfo.setTaskId(taskId);
            taskInfo.setImportTime(DateUtils.nowDate());
            taskInfo.setSrcIndicatorTaskId(taskInfo.getId());
            simulateDataService.save(taskInfo);

            JSONObject compareFromChn = this.templateApi.queryZbCompare(taskInfo.getSrcIndicatorTaskId(), "中国");
            ZbCompareInfo compareInfoChn = compareFromChn.getJSONObject("data").toJavaObject(ZbCompareInfo.class);
            compareInfoChn.setModelId(modelId);
            compareInfoChn.setTaskId(taskId);
            compareInfoChn.setImportTime(DateUtils.nowDate());
            compareInfoChn.setIndicatorTaskId(taskInfo.getId());
            compareInfos.add(compareInfoChn);
            JSONObject compareFromUsa = this.templateApi.queryZbCompare(taskInfo.getSrcIndicatorTaskId(), "美国");
            ZbCompareInfo compareInfoUsa = compareFromUsa.getJSONObject("data").toJavaObject(ZbCompareInfo.class);
            compareInfoUsa.setModelId(modelId);
            compareInfoUsa.setTaskId(taskId);
            compareInfoUsa.setImportTime(DateUtils.nowDate());
            compareInfoUsa.setIndicatorTaskId(taskInfo.getId());
            compareInfos.add(compareInfoUsa);
        }
        this.compareService.insertBatch(compareInfos);
        info.setAchieveCount(info.getAchieveCount() + externalData.size() + compareInfos.size());

        int newDataCount = externalData.size() + compareInfos.size();
        this.modelCommonService.updateSimulsteTaskInfo(taskId, newDataCount);
    }


    /**
     * 通过反射将 DTO 对象转换为查询字符串
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
