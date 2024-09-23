package cn.iecas.simulate.assessment.entity.common;

import lombok.Data;



@Data
public class CommonResult<T> {

    private T data;

    private String code;

    private String message;

    private static final long serialVersionUID = -4683516289108960739L;

    private void code(String code){
        this.code = code;
    }

    public CommonResult<T> message(String message){
        this.message = message;
        return this;
    }

    public CommonResult<T> data(T data){
        this.data = data;
        return this;
    }

    public CommonResult<T> success(){
        code("OK");
        return this;
    }

    public CommonResult<T> fail(ResultCodeEnum codeEnum){
        code(codeEnum.code);
        return this;
    }

    public CommonResult<T> setCode(ResultCodeEnum code){
        this.code = code.code;
        return this;
    }
}
