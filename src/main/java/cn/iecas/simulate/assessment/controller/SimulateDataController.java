package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.entity.common.CommonResult;

import cn.iecas.simulate.assessment.service.SimulateDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;



/**
 * @auther chenyulin
 * @date 2024/8/29
 * @description 模型仿真数据
 */
@RestController
@Api(tags = "仿真数据管理模块")
@RequestMapping(value = "/simulateDataController")
public class SimulateDataController {

    @Autowired
    private SimulateDataService simulateDataService;


    @ApiOperation(value = "获取模型仿真数据引接趋势变化信息")
    @GetMapping("getSimulateImportTrend")
    @ApiImplicitParam(name = "taskId", paramType = "query", value = "仿真任务id", required = true)
    public CommonResult<List<Map<String,Object>>> getImportTimeTrend(@RequestParam Integer taskId) {
        List<Map<String,Object>> result=simulateDataService.getImportTrendByTaskId(taskId);
        return new CommonResult<List<Map<String ,Object>>>().data(result).message("获取模型仿真数据引接趋势变化信息成功");
    }
}
