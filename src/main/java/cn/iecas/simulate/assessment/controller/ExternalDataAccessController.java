package cn.iecas.simulate.assessment.controller;


import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.service.ExternalDataAccessService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Time: 2024/10/9 10:05
 * @Author: guoxun
 * @File: ExternalDataAccessController
 * @Description: 外部数据接引
 */
@RestController
@RequestMapping("/externalDataAccess")
public class ExternalDataAccessController {

    @Autowired
    ExternalDataAccessService externalDataAccessService;


    @Log("根据外部接口地址及查询参数引接数据-测试专用")
    @ApiOperation("根据外部接口地址及查询参数引接数据-测试专用")
    @GetMapping("/getExternalData")
    public CommonResult<Object> getExternalData(ExternalDataDTO dto) throws Exception {
        List<SimulateDataInfo> result = externalDataAccessService.getExternalData(dto);
        return new CommonResult<>().success().message("查询成功").data(result);
    }


    @Log("启动任务线程-持续引接外部数据")
    @ApiOperation("启动任务线程-持续引接外部数据")
    @GetMapping("/startTask")
    public CommonResult<Object> startTask(ExternalDataDTO dto){
        Map<String, Object> result = externalDataAccessService.startTask(dto);
        return new CommonResult<>().success().message("方法调用成功").data(result);
    }


    @Log("终止任务线程")
    @ApiOperation("终止任务线程")
    @GetMapping("/stopTask")
    public CommonResult<Object> stopTask(@RequestParam(required = false) String threadName,
                                         @RequestParam(required = false) Integer taskId){
        Map<String, Object> result = externalDataAccessService.stopTask(threadName, taskId);
        return new CommonResult<>().success().message("方法调用成功").data(result);
    }


    @Log("挂起线程")
    @ApiOperation("挂起线程")
    @GetMapping("/suspendTask")
    public CommonResult<Object> suspendTask(@RequestParam(required = false) String threadName,
                                            @RequestParam(required = false) Integer taskId) throws InterruptedException {
        Map<String, Object> result = externalDataAccessService.suspendTask(threadName, taskId);
        return new CommonResult<>().success().message("方法调用成功").data(result);
    }


    @Log("恢复线程")
    @ApiOperation("恢复线程")
    @GetMapping("/resumeTask")
    public CommonResult<Object> resumeTask(@RequestParam(required = false) String threadName,
                                           @RequestParam(required = false) Integer taskId,
                                           @RequestParam(required = false) Integer frequency,
                                           @RequestParam(required = false) Integer pageSize) throws Exception {
        Map<String, Object> result = externalDataAccessService.resumeTask(threadName, taskId, frequency, pageSize);
        return new CommonResult<>().success().message("方法调用成功").data(result);
    }
}
