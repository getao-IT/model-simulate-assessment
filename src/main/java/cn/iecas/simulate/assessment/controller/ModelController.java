package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.service.ModelService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;



/**
 * @auther cyl
 * @Date 2024/8/19 11:11
 * @Description 模型管理类
 */
@RestController
@RequestMapping("/modelController")
@Api(tags = "模型管理模块")
public class ModelController {

   @Autowired
   private ModelService modelService;


   @Log("获取模型信息")
   @ApiOperation("获取模型信息")
   @GetMapping(value = "/getModelInfo")
   @ApiImplicitParams({
           @ApiImplicitParam(name = "pageSize", paramType = "query", value = "分页大小", required = false, defaultValue = "10"),
           @ApiImplicitParam(name = "pageNo", paramType = "query", value = "第几页", required = false, defaultValue = "1"),
           @ApiImplicitParam(name = "modelName", paramType = "query", value = "模型名称"),
           @ApiImplicitParam(name = "userLevel", paramType = "query", value = "用户层级"),
           @ApiImplicitParam(name = "field", paramType = "query", value = "模型所属领域"),
           @ApiImplicitParam(name = "serviceType", paramType = "query", value = "业务流程类型"),
           @ApiImplicitParam(name = "unit", paramType = "query", value = "单位名称"),
           @ApiImplicitParam(name = "sortField", paramType = "query", value = "排序字段"),
           @ApiImplicitParam(name = "sortOrder", paramType = "query", value = "排序方式"),
           @ApiImplicitParam(name = "vague", paramType = "query", value = "模糊查询字段")
   })
   public CommonResult<IPage<TbModelInfo>> getModelInfo(TbModelInfo tbModelInfo) {
      IPage<TbModelInfo> result = modelService.getModelInfo(tbModelInfo);
      return new CommonResult<IPage<TbModelInfo>>().data(result).message("模型查询成功");
     }


   @Log("更新模型信息")
   @ApiOperation("更新模型信息")
   @PutMapping(value = "/updateModelInfo")
   @ApiImplicitParam(name = "id", paramType = "body", value = "模型id", required = true)
   public CommonResult<TbModelInfo> updateModel(@RequestBody TbModelInfo tbModelInfo){
      modelService.updateModel(tbModelInfo);
      return new CommonResult<TbModelInfo>().success().message("模型更新成功");
   }


   @Log("删除模型信息")
   @ApiOperation("删除模型信息")
   @DeleteMapping(value = "/deleteModelInfo")
   public CommonResult<TbModelInfo> deleteModels(@RequestParam List<Integer> ids){
      modelService.deleteModels(ids);
      return new CommonResult<TbModelInfo>().success().message("删除模型成功"+ids);
   }


   @Log("模型启动状态控制")
   @ApiOperation("模型启动状态控制")
   @PutMapping(value = "/updateModelStatus")
   public CommonResult<TbModelInfo> updateModelStatus(
    @RequestParam("id") Long id,
    @RequestParam(value = "status",required = false) Boolean status) {
      if(id==null){
         return new CommonResult<TbModelInfo>().success().message("获取模型失败!!!");
      }
      if(status==null){
         return new CommonResult<TbModelInfo>().success().message("获取模型失败!!!");
      }
      boolean isUpdated=modelService.updateModelStatus(id,status);
      if (isUpdated){
         return new CommonResult<TbModelInfo>().success().message("模型启动状态变更成功！变更后的状态为："+status);
      }else{
         return new CommonResult<TbModelInfo>().success().message("模型启动状态变更失败");
      }
   }


   @Log("根据模型类别获取模型统计信息")
   @ApiOperation("根据模型类别获取模型统计信息")
   @GetMapping(value = "/getModelByType")
   public CommonResult<Map<String,Long>> getModelCountByServiceType(){
      Map<String,Long> result =  modelService.getServiceTypeByType();
      return new CommonResult<Map<String,Long>>().success().data(result).message("根据模型类别获取模型统计信息查询成功");
   }


   @Log("模型接入")
   @ApiOperation("模型接入")
   @PostMapping(value = "/createModel")
   public CommonResult<TbModelInfo> createModel(@RequestBody TbModelInfo tbModelInfo){
      if(tbModelInfo.getModelName()==null ||tbModelInfo.getUserLevel()==null || tbModelInfo.getField()==null ||tbModelInfo.getServiceType()==null
      ||tbModelInfo.getUnit()==null){
         return new CommonResult<TbModelInfo>().success().message("模型信息获取成功");
      }
      boolean isCreated=modelService.createModel(tbModelInfo);
      if(isCreated){
         return new CommonResult<TbModelInfo>().success().message("模型接入成功");
      }else{
         return new CommonResult<TbModelInfo>().success().message("模型接入失败");
      }
   }


   @Log("根据id获取模型信息")
   @ApiOperation("根据id获取模型信息")
   @GetMapping(value = "/getModelInfoById")
   @ApiImplicitParam(name = "modelId", paramType = "query", value = "模型id", required = true)
   public CommonResult<TbModelInfo> getModelInfoById(int modelId) {
      TbModelInfo modelInfo = modelService.getModelInfoById(modelId);
      return new CommonResult<TbModelInfo>().data(modelInfo).message("根据id获取模型信息成功");
   }


   @Log("查询模型单位")
   @ApiOperation("查询模型单位")
   @GetMapping(value = "/findModelUnits")
   public CommonResult<List<String>> findModelUnits(){
      List<String> result = modelService.findModelUnits();
      return new CommonResult<List<String>>().success().data(result).message("查询模型单位信息成功");
   }
}
