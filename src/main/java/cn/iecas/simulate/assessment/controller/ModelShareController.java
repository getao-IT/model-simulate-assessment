package cn.iecas.simulate.assessment.controller;


import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelShareInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelShareDTO;
import cn.iecas.simulate.assessment.service.ModelShareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public CommonResult<Object> getShareModelInfo(ModelShareDTO dto){
        PageResult<ModelShareInfo> result = modelShareService.getShareModelInfo(dto);
        return new CommonResult<>().message("查询成功").success().data(result);
    }


    @Log("模型评估共享")
    @ApiOperation("模型评估共享")
    @PostMapping(value = "/share")
    public CommonResult<Object> share(@RequestBody Map<String, List<Integer>> taskIdList){
        Map<String, Object> result = modelShareService.share(taskIdList.get("taskIdList"));
        return new CommonResult<>().success().message("模型评估共享成功").data(result);
    }


    @Log("模型评估共享删除")
    @ApiOperation("模型评估共享删除")
    @DeleteMapping(value = "/deleteByIds")
    public CommonResult<Object> delete(@RequestBody Map<String, List<Integer>> ids){
        List<Integer> idList = ids.get("ids");
        modelShareService.delete(idList);
        return new CommonResult<>().success().message("删除成功");
    }
}
