package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import com.alibaba.fastjson.JSONObject;
import java.util.List;



/**
 * @auther getao
 * @Date 2024/8/23 9:11
 * @Description 仿真任务服务接口类
 */
public interface SimulateTaskService {

    SimulateTaskInfo updateSimulateTaskInfo(SimulateTaskInfo taskInfo);

    SimulateTaskInfo saveSimulate(SimulateTaskInfo taskInfo);

    PageResult<SimulateTaskInfo> getSimulateTaskInfo(SimulateTaskInfoDto taskInfoDto);

    Integer batchDeleteSimulateTask(List<Integer> idList);

    List<SimulateDataInfo> getSimulateData(SimulateTaskInfoDto taskInfoDto);

    AssessmentStatisticInfo getSimulateTaskStsc(int taskId);

    SimulateTaskInfo getSimulateTaskInfoById(SimulateTaskInfoDto taskInfoDto);

    JSONObject getModelAssessmentInfo(int taskId);

    void updateTaskStatus(int taskId, int modelId, String op);
}
