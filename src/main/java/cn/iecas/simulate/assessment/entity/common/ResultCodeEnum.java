package cn.iecas.simulate.assessment.entity.common;



/**
 * @auther getao
 * @date 2024/8/14
 * @description 后端接口响应状态码枚举类
 */
public enum ResultCodeEnum {

    SUCCESS(0, "success"),
    FAIL(500, "fail"),
    CREATE_FAIL(180001, "创建操作执行失败！"),
    DELETE_FAIL(180002, "删除操作执行失败！"),
    GET_NULL_FAIL(180003, "查询为空！"),
    UPDATE_FAIL(180004, "更新操作执行失败！");
    
    int code;

    String message;

    ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
