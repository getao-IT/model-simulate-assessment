package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;



/**
 * @auther getao
 * @Date 2024/8/23 9:11
 * @Description 仿真任务服务接口类
 */
public interface SimulateTaskService extends IService<SimulateTaskInfo> {

    SimulateTaskInfo updateSimulateTaskInfo(SimulateTaskInfo taskInfo);

    SimulateTaskInfo saveSimulate(SimulateTaskInfo taskInfo);

    PageResult<SimulateTaskInfo> getSimulateTaskInfo(SimulateTaskInfoDto taskInfoDto);

    Integer batchDeleteSimulateTask(List<Integer> idList);

    List<SimulateDataInfo> getSimulateData(SimulateTaskInfoDto taskInfoDto);

    AssessmentStatisticInfo getSimulateTaskStsc(int taskId);

    SimulateTaskInfo getSimulateTaskInfoById(SimulateTaskInfoDto taskInfoDto);

    JSONArray getModelAssessmentInfo(int taskId);

    void updateTaskStatus(int taskId, int modelId, String op);

    void exportAssessmentReport(int taskId, int modelId);

    void changeTaskStatus(Integer taskId, String status);

    boolean queryIsWait(Integer taskId);


    /**
     * 重启服务时检查task表中的任务状态，并将非WAIT和FINISH的全部设置为FAIL
     */
    void checkStatusAndSetFail();
}
