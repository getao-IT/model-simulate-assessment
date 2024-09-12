package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.service.IndexInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;



/**
 * @auther cyl
 * @Date 2024/8/30 17:35
 * @Description 模型指标信息管理类
 */
@RestController
@RequestMapping("Controller")
@Api(tags = "模型指标管理模块")
public class IndexController {

    @Autowired
    private IndexInfoService indexInfoService;


    @Log("获取模型指标信息")
    @ApiOperation("获取模型指标信息")
    @GetMapping(value = "/getIndexInfo")
    @ApiImplicitParam(name = "sign", paramType = "query", value = "模型标识")
    public CommonResult<Map<String,Object>> getIndexInfo(@RequestParam String sign) {
        Map<String,Object> result = indexInfoService.getIndexInfo(sign);
        return new CommonResult<Map<String,Object>>().data(result).success().message("获取模型指标信息成功");
    }
}
