package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SystemInfo;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.entity.dto.SystemInfoDto;
import cn.iecas.simulate.assessment.service.SystemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;



/**
 * @auther getao
 * @Date 2024/8/21 14:48
 * @Description 信息系统控制类
 */
@RestController
@Api(tags = "信息系统管理模块")
@RequestMapping(value = "/systemController")
public class InfoSystemController {

    @Autowired
    private SystemService systemService;


    @Log("获取信息系统信息")
    @ApiOperation(value = "获取信息系统信息", notes = "分页列表")
    @GetMapping(value = "/getSystemInfo")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", paramType = "query", value = "分页大小", required = false, defaultValue = "10"),
            @ApiImplicitParam(name = "pageNo", paramType = "query", value = "第几页", required = false, defaultValue = "1"),
            @ApiImplicitParam(name = "userLevel", paramType = "query", value = "用户层级，可根据其进行查询"),
            @ApiImplicitParam(name = "systemName", paramType = "query", value = "系统名称，可根据其进行查询"),
            @ApiImplicitParam(name = "systemSign", paramType = "query", value = "系统标识，可根据其进行查询"),
            @ApiImplicitParam(name = "unit", paramType = "query", value = "单位名称，可根据其进行查询"),
            @ApiImplicitParam(name = "leTime", paramType = "query", value = "最大时间，返回该时间为止的数据"),
            @ApiImplicitParam(name = "geTime", paramType = "query", value = "最小时间，返回该时间为始的数据"),
            @ApiImplicitParam(name = "fuzzy", paramType = "query", value = "模糊查询字段"),
            @ApiImplicitParam(name = "orderCol", paramType = "query", value = "排序字段"),
            @ApiImplicitParam(name = "orderWay", paramType = "query", value = "排序方式：asc / desc")
    })
    public CommonResult<PageResult<SystemInfo>> getSystemInfo(SystemInfoDto systemInfoDto){
        PageResult<SystemInfo> systemInfo = systemService.getSystemInfo(systemInfoDto);
        return new CommonResult<PageResult<SystemInfo>>().success().data(systemInfo).message("获取信息系统信息成功");
    }


    @Log("信息系统注册")
    @ApiOperation("信息系统注册")
    @PostMapping(value = "/register")
    public CommonResult<SystemInfo> register(@RequestBody SystemInfo systemInfo){
        SystemInfo result = systemService.saveSystemInfo(systemInfo);
        return new CommonResult<SystemInfo>().success().data(result).message("信息系统注册成功");
    }


    @Log("更新信息系统信息")
    @ApiOperation("更新信息系统信息")
    @PutMapping(value = "/updateSystemInfoById")
    @ApiImplicitParam(value = "id必传，其他信息按需选择")
    public CommonResult<SystemInfo> updateSystemInfo(@RequestBody SystemInfo systemInfo){
        SystemInfo sysInfo = systemService.updateSystemInfo(systemInfo);
        return new CommonResult<SystemInfo>().success().data(sysInfo).message("更新信息系统信息成功");
    }


    @Log("批量删除信息系统信息")
    @ApiOperation("批量删除信息系统信息")
    @DeleteMapping(value = "/batchDeleteSystemInfo")
    public CommonResult<Integer> batchDeleteSystemInfo(@RequestParam List<Integer> idList){
        Integer result = systemService.batchDeleteSystemInfo(idList);
        return new CommonResult<Integer>().success().data(result).message("批量删除信息系统信息成功");
    }


    /**
     * 9.19 13：00 cyl修改，前端要求返回key：value
     * @return
     */
    @Log("查询用户层级信息")
    @ApiOperation("查询用户层级信息")
    @GetMapping(value = "/findUserLevels")
    public CommonResult<List<Map<String,String>>> findUserLevels(){
        List<Map<String,String>> result = systemService.findUserLevels();
        return new CommonResult<List<Map<String,String>>>().success().data(result).message("查询用户层级信息成功");
    }


    /**
     * 9.24 16：00 cyl修改，信息系统启动状态控制
     * @return
     */
    @Log("系统启动状态控制")
    @ApiOperation("系统启动状态控制")
    @PutMapping(value = "/updateSystemStatus")
    public CommonResult<SystemInfo> updateSystemStatus(
            @RequestParam("id") Long id,
            @RequestParam(value = "status",required = false) Boolean status) {
        if(id==null){
            return new CommonResult<SystemInfo>().success().message("获取信息系统失败!!!");
        }
        if(status==null){
            return new CommonResult<SystemInfo>().success().message("获取信息系统失败!!!");
        }
        boolean isUpdated=systemService.updateModelStatus(id,status);
        if (isUpdated){
            return new CommonResult<SystemInfo>().success().message("系统启动状态变更成功！变更后的状态为："+status);
        }else{
            return new CommonResult<SystemInfo>().success().message("系统启动状态变更失败");
        }
    }

}
