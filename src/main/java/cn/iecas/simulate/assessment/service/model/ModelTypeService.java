package cn.iecas.simulate.assessment.service.model;


import cn.iecas.simulate.assessment.service.impl.ExternalDataAccessServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;


/**
 * @auther getao
 * @date 2024/10/30
 * @description 模型类型公共服务类，根据模型类别不同进行不同数据或业务处理
 */
public interface ModelTypeService<T> {

    List<T> requestUrl(Object params) throws Exception;

    void handleExternalData(List<T> externalDataJson, String threadName, Integer offset, ExternalDataAccessServiceImpl.StatusInfo info
            , Integer taskId, Integer modelId);

    JSONObject getSimulateRealData(int taskId, int modelId);

    JSONObject pullSimulateData(int taskId, int modelId);

    JSONObject listModelRealData(int taskId, int modelId);

    JSONObject listModelOutputData(int taskId, int modelId);

    JSONArray startAssessment(int taskId, int modelId, JSONArray assessmentResult, List<Integer> indexSystemList,
                              List<Integer> modelIdList, List<Integer> weightList);

    void exportAssessmentReport(int taskId, int modelId, double contibution);
}
