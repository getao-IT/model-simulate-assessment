package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SceneInfo;
import cn.iecas.simulate.assessment.entity.dto.SceneInfoDto;
import cn.iecas.simulate.assessment.service.SceneService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;



/**
 * @auther cyl
 * @Date 2024/8/22 11:11
 * @Description 作战场景管理类
 */
@RestController
@RequestMapping("/sceneController")
@Api(tags = "作战场景管理模块")
public class SceneController {

   @Autowired
   private SceneService sceneService;


   @Log("获取作战场景信息")
   @ApiOperation("获取作战场景信息")
   @GetMapping(value = "/getSceneInfo")
   @ApiImplicitParams({
           @ApiImplicitParam(name = "pageSize", paramType = "query", value = "分页大小", required = false, defaultValue = "10"),
           @ApiImplicitParam(name = "pageNo", paramType = "query", value = "第几页", required = false, defaultValue = "1"),
           @ApiImplicitParam(name = "sceneName", paramType = "query", value = "场景名称"),
           @ApiImplicitParam(name = "userLevel", paramType = "query", value = "用户层级"),
           @ApiImplicitParam(name = "field", paramType = "query", value = "场景所属领域"),
           @ApiImplicitParam(name = "creater", paramType = "query", value = "创建人"),
           @ApiImplicitParam(name = "cleTime", paramType = "query", value = "最大创建时间，查询至此为止的数据"),
           @ApiImplicitParam(name = "cgeTime", paramType = "query", value = "最小创建时间，查询从此开始的数据"),
           @ApiImplicitParam(name = "mleTime", paramType = "query", value = "最大修改时间，查询至此为止的数据"),
           @ApiImplicitParam(name = "mgeTime", paramType = "query", value = "最小修改时间，查询从此开始的数据"),
           @ApiImplicitParam(name = "sortField", paramType = "query", value = "排序字段"),
           @ApiImplicitParam(name = "sortOrder", paramType = "query", value = "排序方式"),
           @ApiImplicitParam(name = "vague", paramType = "query", value = "模糊查询字段")
   })
   public CommonResult<Object> getSceneInfo(SceneInfoDto sceneInfoDto) {
      IPage<SceneInfo> result = sceneService.getSceneInfo(sceneInfoDto);
      PageResult<SceneInfo> pageResult = new PageResult<>(result.getCurrent(), result.getTotal(), result.getRecords());
      return new CommonResult<>().data(pageResult).message("作战场景查询成功").success();
   }


   @Log("更新作战场景信息")
   @ApiOperation("更新作战场景信息")
   @PutMapping(value = "/updateSceneInfo")
   @ApiImplicitParam(name = "id", paramType = "body", value = "场景id", required = true)
   public CommonResult<SceneInfo> updateScene(@RequestBody SceneInfo sceneInfo){
      sceneService.updateScene(sceneInfo);
      return new CommonResult<SceneInfo>().success().message("作战场景更新成功");
   }


   @Log("删除作战场景信息")
   @ApiOperation("删除作战场景信息")
   @DeleteMapping(value = "/deleteSceneInfo")
   public CommonResult<SceneInfo> deleteModels(@RequestParam List<Integer> ids){
      sceneService.deleteScene(ids);
      return new CommonResult<SceneInfo>().success().message("删除作战场景成功,ids有："+ids);
   }


   @Log("新增作战场景信息")
   @ApiOperation("新增作战场景信息")
   @PostMapping(value = "/addSceneInfo")
   public CommonResult<SceneInfo> addSceneInfo(@RequestBody SceneInfo sceneInfo){
      sceneService.addSceneInfo(sceneInfo);
      return new CommonResult<SceneInfo>().success().message("作战场景新增成功");
   }
}
