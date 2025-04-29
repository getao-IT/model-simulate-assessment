package cn.iecas.simulate.assessment.aop.aspect;

import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.aop.annotation.Log;
import cn.iecas.simulate.assessment.entity.common.ResultCodeEnum;
import com.alibaba.fastjson.JSONObject;
/*import com.cetc54.logSdk.bean.LogDTO;
import com.cetc54.logSdk.enums.OperatorTypeEnum;
import com.cetc54.logSdk.util.LogPutUtil;*/
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


/**
 * @author getao
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Value(value = "${log-record.service-conf.data-id}")
    private String dataId;

    @Value(value = "${log-record.service-conf.service-name}")
    private String serviceName;

    @Value(value = "${log-record.service-conf.ip}")
    private String ip;

    @Value(value = "${log-record.service-conf.index-name}")
    private String indexName;

    @Value(value = "${log-record.enabled}")
    private Boolean enabled;


    @Pointcut("@annotation(cn.iecas.simulate.assessment.aop.annotation.Log)")
    public void pointcut(){
    }


    @Around("pointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //LogDTO logDTO = this.buildLogDto();

        CommonResult<?> result = null;
        String methodName = ((MethodSignature)point.getSignature()).getMethod().getAnnotation(Log.class).value();
        log.info("=============================================================================>>");
        log.info("=============================================================================>>");
        log.info("==>> 进入 {} 的 {} 方法",point.getSignature().getDeclaringType().getName(), methodName);
        Object[] args = point.getArgs();

        for (int i = 0; i < args.length; i++) {
            log.info("参数为: {}", args[i]);
            if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse || args[i] instanceof InputStreamSource)
                args[i] = null;
        }

        //logDTO.setMethodName(methodName);
        String argStr = JSONObject.toJSONString(args);
        int len = argStr.length() < 255 ? argStr.length() : 255;
        //logDTO.setArgs(argStr.substring(0, len));
        try{
            long begin = System.currentTimeMillis();
            result = (CommonResult<?>) point.proceed();
            long timeConsuming = System.currentTimeMillis() - begin;
            log.info("{} 方法执行完毕，返回参数 {}, 共耗时 {} 毫秒", methodName, result,timeConsuming);

            if (enabled) {
                try {
                    String rstStr = JSONObject.toJSONString(result);
                    int rstlen = rstStr.length() < 255 ? rstStr.length() : 255;
                    //logDTO.setReturnStr(rstStr.substring(0, rstlen));
                    //logDTO.setSuccess(true);
                    //logDTO.setLevel("INFO");
                    //log.info("推送日志信息为：{}", logDTO.toString());
                    //LogPutUtil.put(logDTO);
                    log.info("推送正常日志成功...");
                } catch (Throwable e) {
                    log.error("推送日志SDK接口执行失败... {}", e.getMessage());
                }
            }
            log.info("=============================================================================>>\n\n");
        }catch (Exception e){
            result = new CommonResult<>().fail(ResultCodeEnum.FAIL).message(e.getMessage());
            log.error("{} 方法执行异常，返回参数 {}, 异常栈: {}", methodName, result, e.getStackTrace()[0]);

            if (enabled) {
                int rstlen = result.toString().length() < 255 ? result.toString().length() : 255;
                /*logDTO.setReturnStr(result.toString().substring(0, rstlen));
                logDTO.setLevel("ERROR");
                logDTO.setException(e.getMessage());
                logDTO.setExecutionTime(System.currentTimeMillis());
                logDTO.setSuccess(false);*/
                try {
                    /*log.info("推送日志信息为：{}", logDTO.toString());
                    LogPutUtil.put(logDTO);*/
                    log.info("推送异常日志成功...");
                } catch (Throwable e1) {
                    log.error("推送日志SDK接口执行失败... {}", e1.getMessage());
                }
            }

            log.info("=============================================================================>>\n\n");
            throw e;
        }

        return result;
    }


    /**
     * 构建日志信息
     */
    /*private LogDTO buildLogDto() {
        LogDTO logDTO = new LogDTO();
        logDTO.setDataId(dataId);
        logDTO.setServiceName(serviceName);
        logDTO.setIp(ip);
        logDTO.setIndexName(indexName);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String uri = request.getRequestURI();
        String[] uriInfo = uri.split("/");
        logDTO.setModuleName(getController(uriInfo[2]));
        logDTO.setDataType(getController(uriInfo[2])+"-统一数据");
        logDTO.setFirstModuleName(getController(uriInfo[2]));
        logDTO.setFirstDataType(getController(uriInfo[2])+"-一级数据");
        String method = request.getMethod();
        String operatorType = "";
        switch (method) {
            case "GET":
                operatorType = OperatorTypeEnum.QUERY.getType();break;
            case "PUT":
                operatorType = OperatorTypeEnum.UPDATE.getType();break;
            case "DELETE":
                operatorType = OperatorTypeEnum.DELETE.getType();break;
            case "POST":
                operatorType = OperatorTypeEnum.INSERT.getType();break;
        }
        logDTO.setOperatorType(operatorType);
        logDTO.setTimeStamp(System.currentTimeMillis());
        logDTO.setLogId(serviceName + "-" + UUID.randomUUID().toString());

        return logDTO;
    }*/


    /**
     * 获取请求名称
     * @param controller
     * @return
     */
    private String getController(String controller) {
        switch (controller) {
            case "externalDataAccess":
                return "数据引接管理模块";
            case "file":
                return "文件传输管理模块";
            case "Controller":
                return "模型指标管理模块";
            case "indexSystemController":
                return "模型指标体系管理模块";
            case "systemController":
                return "信息系统管理模块";
            case "model":
                return "模型仿真评估记录管理模块";
            case "shareModel":
                return "模型管理模块";
            case "sceneController":
                return "作战场景管理模块";
            case "SimulateAssessment":
                return "模型仿真评估统计管理模块";
            case "simulateDataController":
                return "仿真数据管理模块";
            case "simulateController":
                return "仿真任务管理模块";
            case "testController":
                return "测试管理模块";
        }
        return "未知模块";
    }
}
