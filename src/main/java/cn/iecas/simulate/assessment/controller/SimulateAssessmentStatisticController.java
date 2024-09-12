package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentStatisticInfo;
import cn.iecas.simulate.assessment.service.SimulateAssessmentStatisticService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



/**
 * @auther chenyulin
 * @date 2024/8/28
 * @description 模型仿真评估统计
 */
@RestController
@RequestMapping("/SimulateAssessment")
@Api(tags = "模型仿真评估统计管理模块")
public class SimulateAssessmentStatisticController {

    @Autowired
    private SimulateAssessmentStatisticService simulateAssessmentStatisticService;


    @Log("获取仿真评估结果信息")
    @ApiOperation("获取仿真评估结果信息")
    @GetMapping(value = "/getSimulateAssessmentInfo")
    @ApiImplicitParam(name = "taskId", paramType = "query", value = "仿真任务id", required = true)
    public CommonResult<ModelAssessmentStatisticInfo> getAssessmentResult(@RequestParam Integer taskId) {
        ModelAssessmentStatisticInfo result=simulateAssessmentStatisticService.getSimulateDataByTaskId(taskId);
        return new CommonResult<ModelAssessmentStatisticInfo>().data(result).message("获取仿真评估结果成功");
    }
}
