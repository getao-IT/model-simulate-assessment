package cn.iecas.simulate.assessment.controller;

import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.service.SystemService;
import cn.iecas.simulate.assessment.service.assessment.ModelTypeService;
import cn.iecas.simulate.assessment.service.test.service.DataTestServiceImpl;
import cn.iecas.simulate.assessment.service.test.pojo.SimulateTaskInfoDto;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;


/**
 * @auther getao
 * @Date 2024/8/14 11:11
 * @Description 测试用控制器类
 */
@RestController
@Api(tags = "测试控制器")
@RequestMapping(value = "/testController")
public class TestController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private DataTestServiceImpl testService;


    @Log("TEST")
    @ApiOperation("TEST")
    @GetMapping(value = "/test")
    public CommonResult<String> testApplication(){
        return new CommonResult<String>().success().data(null).message("hello simulate assessment!!!");
    }


    @Log("获取信息系统信息")
    @ApiOperation("获取信息系统信息")
    @GetMapping(value = "/getSystemInfo")
    public CommonResult<Object> getSystemInfo(){
        return new CommonResult<Object>().success().message("获取信息系统信息成功!!!");
    }


    @ApiOperation("获取模型引接数据模拟")
    @PostMapping("/getSimulateData")
    public JSONObject getSimulateData(@RequestBody SimulateTaskInfoDto simulateTaskInfoDto) throws IOException {
        return this.testService.getSimulateData(simulateTaskInfoDto);
    }


    @GetMapping("/getAllIndexIndicatorTask")
    public JSONObject getAllIndexIndicatorTask(String modeltype, Integer pageSize, Integer pageNum) {
        return this.testService.getAllIndexIndicatorTask(modeltype, pageSize, pageNum);
    }


    @GetMapping("/queryZbCompare")
    public JSONObject queryZbCompare(int taskId, String countryCn) {
        return this.testService.queryZbCompare(taskId, countryCn);
    }


    /**
     * 联调时，替换为其他单位的模型运行数据接口路径 TODO getao lt
     */
    @Log("根据输入路径获取模型真实数据-目标检测专用")
    @ApiOperation("根据输入路径获取模型真实数据-目标检测专用")
    @GetMapping("/getRealData")
    public CommonResult<Object> getRealData(int taskId, int modelId, String inputPath, String outputPath, String way, Integer pageSize) {
        JSONObject result = testService.getRealData(taskId, modelId, inputPath, outputPath, way, pageSize);
        return new CommonResult<>().success().message("查询成功").data(result);
    }
}
