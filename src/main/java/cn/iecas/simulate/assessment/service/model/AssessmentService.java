package cn.iecas.simulate.assessment.service.model;

import cn.iecas.simulate.assessment.entity.domain.IndexResultInfo;
import java.util.List;



/**
 * @auther getao
 * @Date 2024/8/30 11:06
 * @Description 仿真数据分析评估服务类
 */
public interface AssessmentService<T> {

    IndexResultInfo getModelAssessmentInfo(List<T> simulateDatas, int indexSystemId, IndexResultInfo resultInfo,
                                           int taskId);
}
