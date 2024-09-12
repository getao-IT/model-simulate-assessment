package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.domain.AssessmentResultInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import java.util.List;



/**
 * @auther getao
 * @Date 2024/8/30 11:06
 * @Description 仿真数据分析评估服务类
 */
public interface SimulateDataAnalysisService {

    AssessmentResultInfo getFHGXFXAssessmentInfo(List<SimulateDataInfo> simulateDatas, int indexSystemId, AssessmentResultInfo resultInfo);
}
