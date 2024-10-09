package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import cn.iecas.simulate.assessment.service.SimulateDataService;
import cn.iecas.simulate.assessment.service.SimulateTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;



/**
 * @auther getao
 * @Date 2024/8/23 8:45
 * @Description 仿真任务控制器类
 */
@RestController
@Api(tags = "仿真任务管理模块")
@RequestMapping(value = "/simulateController")
public class SimulateTaskController {

    @Autowired
    private SimulateTaskService simulateTaskService;

    @Autowired
    private SimulateDataService dataService;


    @Log("获取仿真任务信息")
    @ApiOperation("获取仿真任务信息")
    @GetMapping(value = "/getSimulateTaskInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", paramType = "query", value = "分页大小", required = false, defaultValue = "10"),
            @ApiImplicitParam(name = "pageNo", paramType = "query", value = "第几页", required = false, defaultValue = "1"),
            @ApiImplicitParam(name = "taskName", paramType = "query", value = "仿真任务名称"),
            @ApiImplicitParam(name = "userLevel", paramType = "query", value = "用户层级"),
            @ApiImplicitParam(name = "field", paramType = "query", value = "场景所属领域"),
            @ApiImplicitParam(name = "sceneName", paramType = "query", value = "场景名称"),
            @ApiImplicitParam(name = "modelName", paramType = "query", value = "模型名称"),
            @ApiImplicitParam(name = "creater", paramType = "query", value = "创建人"),
            @ApiImplicitParam(name = "taskType", paramType = "query", value = "任务类型"),
            @ApiImplicitParam(name = "leTime", paramType = "query", value = "最大创建时间，查询至此为止的数据"),
            @ApiImplicitParam(name = "geTime", paramType = "query", value = "最小创建时间，查询从此开始的数据"),
            @ApiImplicitParam(name = "orderCol", paramType = "query", value = "排序字段"),
            @ApiImplicitParam(name = "orderWay", paramType = "query", value = "排序方式"),
            @ApiImplicitParam(name = "fuzzy", paramType = "query", value = "模糊查询字段")
    })
    public CommonResult<PageResult<SimulateTaskInfo>> getSimulateTaskInfo(SimulateTaskInfoDto taskInfoDto) {
        PageResult<SimulateTaskInfo> simulateTaskInfo = simulateTaskService.getSimulateTaskInfo(taskInfoDto);
        return new CommonResult<PageResult<SimulateTaskInfo>>().success().data(simulateTaskInfo).message("获取仿真任务信息成功");
    }


    @Log("根据id获取仿真任务信息")
    @ApiOperation("根据id获取仿真任务信息")
    @GetMapping(value = "/getSimulateTaskInfoById")
    public CommonResult<SimulateTaskInfo> getSimulateTaskInfoById(SimulateTaskInfoDto taskInfoDto) {
        SimulateTaskInfo simulateTaskInfo = simulateTaskService.getSimulateTaskInfoById(taskInfoDto);
        return new CommonResult<SimulateTaskInfo>().success().data(simulateTaskInfo).message("根据id获取仿真任务信息成功");
    }


    @Log("新增仿真任务信息")
    @ApiOperation("新增仿真任务信息")
    @PostMapping(value = "/addSimulate")
    public CommonResult<SimulateTaskInfo> addSimulate(@RequestBody SimulateTaskInfo taskInfo) {
        SimulateTaskInfo result = simulateTaskService.saveSimulate(taskInfo);
        return new CommonResult<SimulateTaskInfo>().success().data(result).message("新增仿真任务成功");
    }


    @Log("更新仿真任务信息")
    @ApiOperation("更新仿真任务信息")
    @PutMapping(value = "/updateSimalateTask")
    @ApiImplicitParam(name = "simulateTaskInfo", value = "只可更新task_name, describe，参考接口文档", paramType = "body", required = true)
    public CommonResult<SimulateTaskInfo> updateSimalateTask(@RequestBody SimulateTaskInfo simulateTaskInfo) {
        SimulateTaskInfo taskInfo = simulateTaskService.updateSimulateTaskInfo(simulateTaskInfo);
        return new CommonResult<SimulateTaskInfo>().success().data(taskInfo).message("更新仿真任务信息成功");
    }


    @Log("批量删除仿真任务信息")
    @ApiOperation("批量删除仿真任务信息")
    @DeleteMapping(value = "/batchDeleteSimulateTask")
    public CommonResult<Integer> batchDeleteSimulateTask(@RequestParam List<Integer> idList) {
        Integer result = simulateTaskService.batchDeleteSimulateTask(idList);
        return new CommonResult<Integer>().success().data(result).message("批量仿真任务信息成功");
    }


    @Log("仿真任务数据导调接口")
    @ApiOperation("仿真任务数据导调接口")
    @GetMapping(value = "/importSimulate")
    @ApiImplicitParam(name = "taskInfoDto", value = "参考接口文档", paramType = "query")
    public CommonResult<List<SimulateDataInfo>> getSimulateData(SimulateTaskInfoDto taskInfoDto) {
        List<SimulateDataInfo> simulateData = simulateTaskService.getSimulateData(taskInfoDto);
        return new CommonResult<List<SimulateDataInfo>>().success().data(simulateData).message("获取仿真任务数据成功");
    }


    @Log("获取仿真引接数据接口")
    @ApiOperation("获取仿真引接数据接口")
    @GetMapping(value = "/listSimulateData")
    public CommonResult<PageResult<SimulateDataInfo>> listSimulateData(SimulateDataInfoDto dataInfoDto) {
        PageResult<SimulateDataInfo> dataInfo = dataService.listSimulateData(dataInfoDto);
        return new CommonResult<PageResult<SimulateDataInfo>>().success().data(dataInfo).message("获取仿真引接数据成功");
    }


    @Log("更新模型记录以及仿真任务状态")
    @ApiOperation("更新模型记录以及仿真任务状态")
    @PutMapping(value = "/updateTaskStatus")
    public CommonResult<Object> updateTaskStatus(int taskId, int modelId, String op) {
        simulateTaskService.updateTaskStatus(taskId, modelId, op);
        return new CommonResult<Object>().success().message("更新模型记录以及仿真任务状态成功");
    }


    @Log("获取仿真任务统计信息")
    @ApiOperation("获取模仿真任务统计信息")
    @GetMapping(value = "/getSimulateTaskStsc")
    @ApiImplicitParam(name = "taskId", paramType = "query", value = "仿真任务id", required = true)
    public CommonResult<AssessmentStatisticInfo> getSimulateTaskStsc(int taskId) {
        AssessmentStatisticInfo stscInfo = simulateTaskService.getSimulateTaskStsc(taskId);
        return new CommonResult<AssessmentStatisticInfo>().success().data(stscInfo).message("根据id获取模仿真任务统计信息成功");
    }


    @Log("获取模型评估结果信息")
    @ApiOperation("获取模型评估结果信息")
    @GetMapping(value = "/getModelAssessmentInfo")
    @ApiImplicitParam(name = "taskId", paramType = "query", value = "仿真任务id", required = true)
    public CommonResult<JSONArray> getModelAssessmentInfo(int taskId) {
        JSONArray result = simulateTaskService.getModelAssessmentInfo(taskId);
        return new CommonResult<JSONArray>().success().data(result).message("获取模型评估结果信息成功");
    }


    @Log("导出模型评估报告")
    @ApiOperation("导出模型评估报告")
    @GetMapping(value = "/exportAssessmentReport")
    @ApiImplicitParam(name = "taskId", paramType = "query", value = "仿真任务id", required = true)
    public void exportAssessmentReport(int taskId, int modelId) {
        simulateTaskService.exportAssessmentReport(taskId, modelId);
        //return new CommonResult<JSONObject>().success().data(null).message("导出模型评估报告成功");
    }
}
