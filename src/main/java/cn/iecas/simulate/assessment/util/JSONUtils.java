package cn.iecas.simulate.assessment.util;

import com.alibaba.fastjson.JSONObject;
import java.util.Iterator;



/**
 * @auther getao
 * @date 2024/9/2 17:09
 * @description JSON工具类
 */
public class JSONUtils {


   /**
    * @Description 计算Json串所有包含数字类型的属性的和与个数
    * @auther getao
    * @Date 2024/9/3 10:31
    * @Param [jsonObject, result]
    * @Return com.alibaba.fastjson.JSONObject
    */
    public static JSONObject getJsonValueSum(JSONObject jsonObject, JSONObject result) {
        JSONObject object = new JSONObject();
        object.put("sum", 0);
        object.put("count", 0);
        Iterator<String> keys = jsonObject.keySet().iterator();
        while (keys.hasNext()) {
            Object value = jsonObject.get(keys.next());
            if (value instanceof Number) {
                result.put("sum", result.getDoubleValue("sum") + ((Number) value).doubleValue()) ;
                if (result.getInteger("count") == null) {
                    result.put("count", 0);
                }
                result.put("count", result.getInteger("count") + 1) ;
            }
            if (value instanceof JSONObject) {
                result = getJsonValueSum((JSONObject) value, result);
            }
        }
        if (result.getDoubleValue("count") == 0) {
            return object;
        }
        return result;
    }
}
