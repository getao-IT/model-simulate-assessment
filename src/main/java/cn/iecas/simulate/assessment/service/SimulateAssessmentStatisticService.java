package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentStatisticInfo;



/**
 * @auther chenyulin
 * @date 2024/8/28
 * @description 模型仿真评估统计
 */
public interface SimulateAssessmentStatisticService {
    ModelAssessmentStatisticInfo getSimulateDataByTaskId(Integer taskId);
}
