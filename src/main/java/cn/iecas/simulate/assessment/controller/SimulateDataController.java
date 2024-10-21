package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;

import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.service.SimulateDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public CommonResult<Map<String, Long>> getImportTimeTrend(@RequestParam Integer taskId) {
        Map<String, Long> result = simulateDataService.getImportTrendByTaskId(taskId);
        return new CommonResult<Map<String, Long>>().success().data(result).message("获取模型仿真数据引接趋势变化信息成功");
    }


    /**
     * 此部分内容为测试使用, 生产环境可删除
     */
    @ApiOperation(value = "分页获取所有数据")
    @GetMapping("")
    @Log("分页获取所有数据")
    public CommonResult<Object> getInfo(ExternalDataDTO dto) throws InterruptedException {
        System.out.println("model-name = " + dto.getModelName());
        IPage<SimulateDataInfo> page = simulateDataService.page(new Page<>(dto.getPageNum(), dto.getPageSize())
                , new QueryWrapper<SimulateDataInfo>().eq("model_id", dto.getModelId())
                        .eq("task_id", 1).orderByAsc("id"));
        PageResult<SimulateDataInfo> result = new PageResult<>(page.getCurrent(), page.getTotal(), page.getRecords());
        return new CommonResult<>().message("请求成功").success().data(result);
    }
}
