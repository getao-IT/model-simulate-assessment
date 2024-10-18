package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.IndexSystemInfo;
import cn.iecas.simulate.assessment.entity.dto.IndexSystemInfoDto;
import cn.iecas.simulate.assessment.service.IndexSystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;



/**
 * @auther cyl
 * @Date 2024/8/23 11:11
 * @Description 模型指标体系管理类
 */
@RestController
@RequestMapping("/indexSystemController")
@Api(tags = "模型指标体系管理模块")
public class IndexSyetemController {

   @Autowired
   private IndexSystemService indexSystemService;


   @Log("获取模型指标体系信息")
   @ApiOperation("获取模型指标体系信息")
   @GetMapping(value = "/getIndexSystemInfo")
   @ApiImplicitParams({
           @ApiImplicitParam(name = "pageSize", paramType = "query", value = "分页大小", required = false, defaultValue = "10"),
           @ApiImplicitParam(name = "pageNo", paramType = "query", value = "第几页", required = false, defaultValue = "1"),
           @ApiImplicitParam(name = "indexSystemName", paramType = "query", value = "指标体系名称"),
           @ApiImplicitParam(name = "modelName", paramType = "query", value = "模型名称"),
           @ApiImplicitParam(name = "creater", paramType = "query", value = "建立着"),
           @ApiImplicitParam(name = "vague", paramType = "query", value = "模糊查询字段")
   })
   public CommonResult<PageResult<IndexSystemInfo>> getIndexSystemInfo(IndexSystemInfoDto indexSystemInfoDto) {
      PageResult<IndexSystemInfo> result = indexSystemService.getIndexSystemInfo(indexSystemInfoDto);
      return new CommonResult<PageResult<IndexSystemInfo>>().success().data(result).message("模型指标体系查询成功");
   }


   @Log("新增指标体系信息")
   @ApiOperation("新增指标体系信息")
   @PostMapping(value = "/addIndexSystemInfo")
   @ApiImplicitParams({
           @ApiImplicitParam(name = "modelId", paramType = "body", value = "模型id", required = true),
           @ApiImplicitParam(name = "modelName", paramType = "body", value = "模型名称", required = true)
   })
   public CommonResult<IndexSystemInfo> addIndexSystemInfo(@RequestBody @Valid IndexSystemInfo indexSystemInfo){
       IndexSystemInfo info = indexSystemService.addIndexSystemInfo(indexSystemInfo);
       return new CommonResult<IndexSystemInfo>().success().data(info).message("指标体系新增成功");
   }


   @Log("更新指标体系信息")
   @ApiOperation("更新指标体系信息")
   @PutMapping(value = "/updateIndexSystemInfo")
   @ApiImplicitParam(name = "id", paramType = "body", value = "指标体系id", required = true)
   public CommonResult<IndexSystemInfo> updateIndexSystemInfo(@RequestBody IndexSystemInfo indexSystemInfo){
      indexSystemService.updateIndexSystemInfo(indexSystemInfo);
      return new CommonResult<IndexSystemInfo>().success().message("指标体系更新成功");
   }


   @Log("删除指标体系信息")
   @ApiOperation("删除指标体系信息")
   @DeleteMapping(value = "/deleteIndexSystemInfo")
   public CommonResult<IndexSystemInfo> deleteIndexSystemInfo(@RequestParam List<Integer> ids){
      indexSystemService.deleteIndexSystemInfo(ids);
      return new CommonResult<IndexSystemInfo>().success().message("删除指标体系成功");
   }


   @Log("还原默认指标体系信息")
   @ApiOperation("还原默认指标体系信息")
   @PutMapping("/reset-default")
   public CommonResult<IndexSystemInfo> restore(@RequestParam String sqlFileName){
      indexSystemService.restore(sqlFileName);
      return new CommonResult<IndexSystemInfo>().success().message("还原默认指标体系成功");
   }


  /**
    * @auther cyl
    * @Date 2024/9/2 9:11
    * @Description 根据id查询四级指标信息集合
    */
   @Log("根据id查询四级指标集合信息")
   @ApiOperation("根据id查询四级指标集合信息")
   @GetMapping("/searchIndexes")
   public CommonResult<List<String>> getIndexList(@RequestParam Integer id){
      List<String> result=indexSystemService.getIndexList(id);
      return new CommonResult<List<String>>().data(result).success().message("根据id查询四级指标信息成功");
   }


  /**
    * @auther cyl
    * @Date 2024/9/2 9:11
    * @Description 根据modelId查询4个指标以及各自包含的指标名称
    */
   @Log("根据modelId查询4个指标包含的指标信息")
   @ApiOperation("根据modelId查询4个指标包含的指标信息")
   @GetMapping("/getIndexesByModelId")
   public CommonResult<List<Map<String,List<String>>>> getIndexesByModelId(@RequestParam Integer modelId){
      List<Map<String,List<String>>> result=indexSystemService.getIndexesByModelId(modelId);
      return new CommonResult<List<Map<String,List<String>>>>().data(result).success().message("根据id查询四级指标信息成功");
   }
}
