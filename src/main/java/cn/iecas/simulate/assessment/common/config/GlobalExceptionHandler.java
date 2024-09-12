package cn.iecas.simulate.assessment.common.config;

import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.ResultCodeEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.ResourceAccessException;
import javax.validation.ConstraintViolationException;



@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 处理请求参数异常
     * @param e
     * @return
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<String> handlerBindException(BindException e) {
        String message = "请检查请求参数";
        if (e.getBindingResult().getAllErrors().size()!=0)
            message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new CommonResult<String>().message(message).fail(ResultCodeEnum.FAIL);
    }


    /**
     * 处理请求参数异常
     * @param e
     * @return
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<String> handlerBindException(ConstraintViolationException e) {
          String message = "请检查请求参数";
        if (!e.getConstraintViolations().isEmpty())
            message = e.getConstraintViolations().iterator().next().getMessage();
        return new CommonResult<String>().message(message).fail(ResultCodeEnum.FAIL);
    }


    /**
     * 处理非法参数异常
     * @param e
     * @return
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<String> handlerIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();
        return new CommonResult<String>().message(message).fail(ResultCodeEnum.FAIL);
    }


    /**
     * 处理api调用超时异常
     * @param e
     * @return
     */
    @ExceptionHandler(ResourceAccessException.class)
    public CommonResult<String> handlerResourceAccessException(ResourceAccessException e) {
        String message = e.getMessage();
        return new CommonResult<String>().message(message).fail(ResultCodeEnum.FAIL);
    }
}
