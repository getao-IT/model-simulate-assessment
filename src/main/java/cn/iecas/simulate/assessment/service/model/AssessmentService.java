package cn.iecas.simulate.assessment.service.model;

import cn.iecas.simulate.assessment.entity.domain.AssessmentResultInfo;
import java.util.List;



/**
 * @auther getao
 * @Date 2024/8/30 11:06
 * @Description 仿真数据分析评估服务类
 */
public interface AssessmentService<T> {

    AssessmentResultInfo getModelAssessmentInfo(List<T> simulateDatas, int indexSystemId, AssessmentResultInfo resultInfo);
}
