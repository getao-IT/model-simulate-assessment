package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.entity.domain.AssessmentResultInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.service.SimulateDataAnalysisService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * @auther getao
 * @date 2024/8/30 11:04
 * @description 仿真数据分析评估服务实现类
 */
@Service
public class SimulateDataAnalysisServiceImpl implements SimulateDataAnalysisService {

    @Autowired
    private IndexAssessmentImpl indexAssessment;


   /**
    * @Description 获取府会关系分析模型评估结果
    * @auther getao
    * @Date 2024/8/30 11:15
    * @Param [indexSystemId]
    * @Return
    */
    @Override
    public AssessmentResultInfo getFHGXFXAssessmentInfo(List<SimulateDataInfo> simulateDatas, int indexSystemId, AssessmentResultInfo resultInfo) {
        if (simulateDatas != null && simulateDatas.size() != 0) {
            resultInfo = indexAssessment.analysisFromFHGXFX(simulateDatas, indexSystemId, resultInfo);
        } else {
            JSONArray firstIndex = new JSONArray();
            JSONObject usability = new JSONObject();
            usability.put("name", "可用性");
            usability.put("value", "不可用");
            JSONObject realTime = new JSONObject();
            realTime.put("name", "数据实时性");
            realTime.put("value", "无数据");
            JSONObject conformity = new JSONObject();
            conformity.put("name", "接口符合性");
            conformity.put("value", "不符合");
            JSONObject normative = new JSONObject();
            normative.put("name", "格式规范性");
            normative.put("value", "不规范");
            firstIndex.add(usability);
            firstIndex.add(realTime);
            firstIndex.add(conformity);
            firstIndex.add(normative);
            resultInfo.setFirstIndex(firstIndex);
            resultInfo.setScore(0.0);
            resultInfo.setContibution(0.0);
        }

        return resultInfo;
    }


   /**
    * @Description 获取府会议案类型信息
    * @auther getao
    * @Date 2024/8/30 11:22
    * @Param [dataInfos]
    * @Return
    */
    private List<String> getTerritorys(List<SimulateDataInfo> dataInfos) {
        Map<String, List<SimulateDataInfo>> territoryGroups = dataInfos.stream()
                .collect(Collectors.groupingBy(e -> e.getTerritory()));
        return null;
    }
}
