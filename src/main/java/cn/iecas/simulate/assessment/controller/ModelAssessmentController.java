package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelAssessmentDto;
import cn.iecas.simulate.assessment.service.ModelAssessmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



/**
 * @auther guoxun
 * @Date 2024/8/27 16:59
 * @Description 模型仿真评估记录类
 */
@Api(tags = "模型仿真评估记录管理模块")
@RestController
@RequestMapping("/model")
public class ModelAssessmentController {

    @Autowired
    private ModelAssessmentService modelAssessmentService;


    @Log("获取模型评估记录")
    @ApiOperation("获取模型评估记录")
    @GetMapping(value = "/getAssessmentHistory")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", paramType = "query", value = "分页大小", required = false, defaultValue = "10"),
            @ApiImplicitParam(name = "pageNo", paramType = "query", value = "第几页", required = false, defaultValue = "1"),
            @ApiImplicitParam(name = "sceneName", paramType = "query", value = "场景名称"),
            @ApiImplicitParam(name = "unit", paramType = "query", value = "单位名称"),
            @ApiImplicitParam(name = "modelName", paramType = "query", value = "模型名称"),
            @ApiImplicitParam(name = "creater", paramType = "query", value = "建立者"),
            @ApiImplicitParam(name = "status", paramType = "query", value = "评估状态"),
            @ApiImplicitParam(name = "geCreateTime", paramType = "query", value = "最小创建时间"),
            @ApiImplicitParam(name = "leCreateTime", paramType = "query", value = "最大创建时间"),
            @ApiImplicitParam(name = "geFinishTime", paramType = "query", value = "最小完成时间"),
            @ApiImplicitParam(name = "leFinishTime", paramType = "query", value = "最大完成时间"),
            @ApiImplicitParam(name = "orderCol", paramType = "query", value = "排序字段"),
            @ApiImplicitParam(name = "orderWay", paramType = "query", value = "排序方式")
    })
    public CommonResult<Object> getAssessmentHistory(ModelAssessmentDto modelAssessmentDto){
       PageResult<ModelAssessmentInfo> result =  modelAssessmentService.getAssessmentHistory(modelAssessmentDto);
       return new CommonResult<>().data(result).success().message("获取模型评估记录成功");
    }


    @Log("获取模型应用统计信息")
    @ApiOperation("获取模型应用统计信息")
    @GetMapping(value = "/getModelStatistic")
    public CommonResult<Map<String,Integer>> getEvaluationResult(){
        Map<String,Integer> result=new HashMap<>();
        result.put("modelTotal",modelAssessmentService.getTotalModels());
        result.put("successModels",modelAssessmentService.getFinishModels());
        result.put("failModels",modelAssessmentService.getUnfinishedModels());
        result.put("totalEvaluations",modelAssessmentService.getTotalEvaluations());
        return new CommonResult<Map<String,Integer>>().data(result).success().message("获取模型应用统计信息成功");
    }


    @Log("根据模型类别获取模型评估信息")
    @ApiOperation("根据模型类别获取模型评估信息")
    @GetMapping(value = "/getModelApplyStatistic")
    public CommonResult<Object> getModelApplyStatistic(){
        Map<String, List<Map<String, Object>>> result = modelAssessmentService.getModelApplyStatistic();
        return new CommonResult<>().data(result.get("result")).success().message("获取模型评估信息成功");
    }


    @Log("根据模型类别获取模型应用趋势信息")
    @ApiOperation("根据模型类别获取模型应用趋势信息")
    @GetMapping(value = "/getModelApplyTrend")
    public CommonResult<Object> getModelApplyTrend(@RequestParam String mode){
        List<Map<String, Object>> result = modelAssessmentService.getModelApplyTrend(mode);
        return new CommonResult<>().data(result).success().message("获取模型应用趋势信息成功");
    }
}
