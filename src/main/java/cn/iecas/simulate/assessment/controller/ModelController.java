package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.ResultCodeEnum;
import cn.iecas.simulate.assessment.entity.domain.ModelInfo;
import cn.iecas.simulate.assessment.service.ModelService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;



/**
 * @auther cyl
 * @Date 2024/8/19 11:11
 * @Description 模型管理类
 */
@RestController
@RequestMapping("/model")
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
   public CommonResult<IPage<ModelInfo>> getModelInfo(ModelInfo modelInfo) {
      IPage<ModelInfo> result = modelService.getModelInfo(modelInfo);
      return new CommonResult<IPage<ModelInfo>>().success().data(result).message("模型查询成功");
     }


   @Log("更新模型信息")
   @ApiOperation("更新模型信息")
   @PutMapping(value = "/updateModelInfo")
   @ApiImplicitParam(name = "id", paramType = "body", value = "模型id", required = true)
   public CommonResult<ModelInfo> updateModel(@RequestBody ModelInfo modelInfo){
      modelService.updateModel(modelInfo);
      return new CommonResult<ModelInfo>().success().message("模型更新成功");
   }


   @Log("删除模型信息")
   @ApiOperation("删除模型信息")
   @DeleteMapping(value = "/deleteModelInfo")
   public CommonResult<ModelInfo> deleteModels(@RequestParam List<Integer> ids){
      modelService.deleteModels(ids);
      return new CommonResult<ModelInfo>().success().message("删除模型成功"+ids);
   }


   @Log("模型启动状态控制")
   @ApiOperation("模型启动状态控制")
   @PutMapping(value = "/updateModelStatus")
   public CommonResult<ModelInfo> updateModelStatus(
    @RequestParam("id") Long id,
    @RequestParam(value = "status",required = false) Boolean status) {
      if(id==null){
         return new CommonResult<ModelInfo>().fail(ResultCodeEnum.FAIL).message("获取模型失败!!!");
      }
      if(status==null){
         return new CommonResult<ModelInfo>().fail(ResultCodeEnum.FAIL).message("获取模型失败!!!");
      }
      boolean isUpdated=modelService.updateModelStatus(id,status);
      if (isUpdated){
         return new CommonResult<ModelInfo>().success().message("模型启动状态变更成功！变更后的状态为："+status);
      }else{
         return new CommonResult<ModelInfo>().fail(ResultCodeEnum.FAIL).message("模型启动状态变更失败");
      }
   }


   @Log("根据模型类别获取模型统计信息")
   @ApiOperation("根据模型类别获取模型统计信息")
   @GetMapping(value = "/getModelByType")
   public CommonResult<List<Map<String, Object>>> getModelCountByServiceType(){
      List<Map<String, Object>> result =  modelService.getServiceTypeByType();
      return new CommonResult<List<Map<String, Object>>>().success().data(result).message("根据模型类别获取模型统计信息查询成功");
   }


   @Log("模型注册")
   @ApiOperation("模型注册")
   @PostMapping(value = "/createModel")
   public CommonResult<ModelInfo> createModel(@RequestBody ModelInfo modelInfo){
      if(modelInfo.getModelName() == null || modelInfo.getAssessmentUrl() == null
              || modelInfo.getField() == null || modelInfo.getServiceType() == null || modelInfo.getSystemId() == 0
              || modelInfo.getIndexInfos() == null || modelInfo.getRunUrl() == null
              || modelInfo.getModelType() == null){
         return new CommonResult<ModelInfo>().fail(ResultCodeEnum.FAIL).message("模型信息不完整，无法注册");
      }
      ModelInfo isCreated = modelService.createModel(modelInfo);
      if(isCreated != null){
         return new CommonResult<ModelInfo>().data(isCreated).success().message("模型注册成功");
      }else{
         return new CommonResult<ModelInfo>().data(isCreated).fail(ResultCodeEnum.FAIL).message("模型注册失败");
      }
   }


   @Log("同步模型")
   @ApiOperation("同步模型")
   @PostMapping(value = "/syncModel")
   public CommonResult<ModelInfo> syncModel(@RequestBody ModelInfo modelInfo){
      if(modelInfo.getModelName() == null || modelInfo.getSign() == null || modelInfo.getVersion() == null){
         return new CommonResult<ModelInfo>().fail(ResultCodeEnum.FAIL).message("模型信息不完整，无法同步");
      }
      ModelInfo isCreated = modelService.syncModel(modelInfo);
      if(isCreated != null){
         return new CommonResult<ModelInfo>().data(isCreated).success().message("同步模型成功");
      }else{
         return new CommonResult<ModelInfo>().data(isCreated).fail(ResultCodeEnum.FAIL).message("同步模型失败");
      }
   }


   @Log("根据id获取模型信息")
   @ApiOperation("根据id获取模型信息")
   @GetMapping(value = "/getModelInfoById")
   @ApiImplicitParam(name = "modelId", paramType = "query", value = "模型id", required = true)
   public CommonResult<ModelInfo> getModelInfoById(int modelId) {
      ModelInfo modelInfo = modelService.getModelInfoById(modelId);
      return new CommonResult<ModelInfo>().success().data(modelInfo).message("根据id获取模型信息成功");
   }


   @Log("查询模型单位")
   @ApiOperation("查询模型单位")
   @GetMapping(value = "/findModelUnits")
   public CommonResult<List<String>> findModelUnits(){
      List<String> result = modelService.findModelUnits();
      return new CommonResult<List<String>>().success().data(result).message("查询模型单位信息成功");
   }


   @Log("更改模型可见性")
   @ApiOperation("更改模型可见性")
   @PutMapping(value = "/updateModelVisible")
   public CommonResult<Object> updateModelVisible(@RequestParam Long id, @RequestParam Boolean visible){
      modelService.updateModelVisible(id, visible);
      return new CommonResult<>().success().message("更改模型状态成功").data("当前系统状态为: " + visible);
   }


   @Log("获取模型领域信息")
   @ApiOperation("获取模型领域信息")
   @GetMapping(value = "/getFieldFromModel")
   public CommonResult<Collection<String>> getFieldFromModel(){
      Collection<String> result = modelService.getFieldFromModel();
      return new CommonResult<Collection<String>>().success().data(result).message("获取模型领域信息");
   }


   @Log("获取模型业务类型信息")
   @ApiOperation("获取模型业务类型信息")
   @GetMapping(value = "/getServiceTypeFromModel")
   public CommonResult<Collection<String>> getServiceTypeFromModel(){
      Collection<String> result = modelService.getServiceTypeFromModel();
      return new CommonResult<Collection<String>>().success().data(result).message("获取模型业务类型信息");
   }



   @Log("获取模型类型")
   @ApiOperation("获取模型类型")
   @GetMapping(value = "/getModelType")
   public CommonResult<Collection<String>> getModelType(){
      Collection<String> result = modelService.getModelType();
      return new CommonResult<Collection<String>>().success().data(result).message("获取模型类型成功");
   }


   @Log("判断模型标识是否重名")
   @ApiOperation("判断模型标识是否重名")
   @GetMapping(value = "/checkModelSign")
   @ApiImplicitParam(name = "sign", paramType = "params", value = "模型标识", required = true)
   public CommonResult<Boolean> checkModelSign(String sign){
      Boolean result = modelService.checkModelSign(sign);
      return new CommonResult<Boolean>().success().data(result).message("模型标识校验成功");
   }
}
