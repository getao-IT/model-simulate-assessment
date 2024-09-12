package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.entity.domain.AssessmentResultInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.service.SimulateDataAnalysisService;
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
            JSONObject first = new JSONObject();
            first.put("可用性", "不可用");
            first.put("数据实时性", "无数据");
            first.put("接口符合性", "不符合");
            first.put("格式规范性", "不规范");
            resultInfo.setFourIndex(first);
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
