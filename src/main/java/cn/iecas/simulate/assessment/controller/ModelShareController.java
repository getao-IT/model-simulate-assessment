package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelShareInfo;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelShareDTO;
import cn.iecas.simulate.assessment.service.ModelShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @Time: 2024/10/29 11:23
 * @Author: guoxun
 * @File: ModelShareController
 * @Description:
 */
@RequestMapping("/shareModel")
@RestController
@Api(tags = "模型共享模块")
public class ModelShareController {

    @Autowired
    private ModelShareService modelShareService;


    @Log("获取共享模型评估信息")
    @ApiOperation("获取共享模型评估信息")
    @GetMapping(value = "/getShareModelInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", paramType = "query", value = "分页大小", defaultValue = "10"),
            @ApiImplicitParam(name = "pageNo", paramType = "query", value = "第几页", defaultValue = "1"),
            @ApiImplicitParam(name = "modelName", paramType = "query", value = "模型名称"),
            @ApiImplicitParam(name = "unit", paramType = "query", value = "单位名称"),
            @ApiImplicitParam(name = "fuzzy", paramType = "query", value = "模糊查询字段"),
            @ApiImplicitParam(name = "leTime", paramType = "query", value = "最大時間"),
            @ApiImplicitParam(name = "geTime", paramType = "query", value = "最小時間"),
            @ApiImplicitParam(name = "orderCol", paramType = "query", value = "排序字段"),
            @ApiImplicitParam(name = "orderWay", paramType = "query", value = "排序方式")

    })
    public CommonResult<Object> getShareModelInfo(ModelShareDTO dto){
        PageResult<ModelShareInfo> result = modelShareService.getShareModelInfo(dto);
        return new CommonResult<>().message("查询成功").success().data(result);
    }


    @Log("模型评估共享")
    @ApiOperation("模型评估共享")
    @GetMapping(value = "/share")
    @ApiImplicitParam(name = "idList", paramType = "query", required = true, value = "任務id集合")
    public CommonResult<Object> share(@RequestParam List<Integer> idList){
        Map<String, Object> result = modelShareService.share(idList);
        return new CommonResult<>().success().message("模型评估共享成功").data(result);
    }


    @Log("模型评估共享删除")
    @ApiOperation("模型评估共享删除")
    @DeleteMapping(value = "/deleteByIds")
    @ApiImplicitParam(name = "ids", paramType = "query", required = true, value = "共享id集合")
    public CommonResult<Object> delete(@RequestParam List<Integer> ids){
        modelShareService.delete(ids);
        return new CommonResult<>().success().message("删除成功");
    }


    @Log("模型评估共享统计信息")
    @ApiOperation("模型评估共享统计信息")
    @GetMapping(value = "/getModelShareStatistics")
    public CommonResult<Map<String,Integer>> getModelShareStatistics(){
        Map<String,Integer> result=new HashMap<>();
        result.put("assessmentTotal:",modelShareService.getAssessmentTotal());
        result.put("modelTotal:",modelShareService.getModelTotal());
        return new CommonResult<Map<String,Integer>>().data(result).success().message("模型评估共享统计信息");
    }


    @Log("模型评估类别")
    @ApiOperation("模型评估类别")
    @GetMapping(value = "/getModelAssessmentCategory")
    public CommonResult<Map<String, Object>> getModelAssessmentCategory() {
        Map<String, Object> result=modelShareService.getModelStatistics();
        return new CommonResult<Map<String,Object>>().data(result).success().message("模型评估共享统计信息");
    }


    @Log("模型评估記錄类别统计")
    @ApiOperation("模型评估記錄类别统计")
    @GetMapping(value = "/getModelAssessmentType")
    public CommonResult<List<TbModelInfo>> getModelAssessmentType() {
        List<TbModelInfo> result = modelShareService.getModelAssessmentType();
        return new CommonResult<List<TbModelInfo>>().data(result).success().message("模型评估記錄類別统计信息");
    }
}
