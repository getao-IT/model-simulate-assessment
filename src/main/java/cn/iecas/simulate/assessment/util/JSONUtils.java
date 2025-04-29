package cn.iecas.simulate.assessment.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


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
                result.put("sum", result.getDoubleValue("sum") + ((Number) value).doubleValue());
                if (result.getInteger("count") == null) {
                    result.put("count", 0);
                }
                result.put("count", result.getInteger("count") + 1);
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


    /**
     * @return com.alibaba.fastjson.JSONObject
     * @Description 合并同格式json并计算整数类型平均值
     * @Author getao
     * @Date 11:04 2025/3/20
     * @Param [jsonList, weight]
     */
    public static String mergeJsonAndCalculate(List<JSONObject> jsonList) throws JsonProcessingException {
        List<String> jsonStrs = jsonList.stream().map(JSONObject::toString).collect(Collectors.toList());
        return mergeJsonStrAndCalculate(jsonStrs);
    }


    /**
     * @return com.alibaba.fastjson.JSONObject
     * @Description 合并同格式json字符串并计算整数类型平均值
     * @Author getao
     * @Date 11:04 2025/3/20
     * @Param [jsonList]
     */
    public static String mergeJsonStrAndCalculate(List<String> jsonList) throws JsonProcessingException {
        String mergeJsons = mergeJsons(jsonList);
        return mergeJsons;
    }


    private static String mergeJsons(List<String> jsonList) {
        ObjectMapper mapper = new ObjectMapper();
        if (jsonList == null || jsonList.isEmpty()) {
            return "{}";
        }

        ObjectNode mergedJson = mapper.createObjectNode();
        int count = jsonList.size();

        for (String json : jsonList) {
            try {
                JsonNode jsonNode = mapper.readTree(json);
                double weight = 100;
                if (jsonNode.has("weight"))
                    weight = jsonNode.get("weight").asDouble();
                merge(mergedJson, jsonNode, count, weight);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            return mapper.writeValueAsString(mergedJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "{}";
    }


    private static void merge(ObjectNode mergedJson, JsonNode jsonNode, int count, double weight) {
        ObjectMapper mapper = new ObjectMapper();
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            JsonNode value = entry.getValue();
            String key = entry.getKey();

            if (value.isNumber() && key.equalsIgnoreCase("score")) {
                double currentValue = mergedJson.has(key) ? mergedJson.get(key).asDouble() : 0.0;
                double sorce = 0;
                if (weight == 100) {
                    sorce = currentValue + value.asDouble() * (weight / 100.0) / count;
                } else {
                    sorce = currentValue + value.asDouble() * (weight / 100.0);
                }
                mergedJson.put(key, MathUtils.halfUpScale2(sorce));
            } else if (value.isObject()) {
                ObjectNode mergedObject = mergedJson.has(key) ? (ObjectNode) mergedJson.get(key) : mapper.createObjectNode();
                merge(mergedJson, value, count, weight);
                mergedJson.set(key, mergedObject);
            } else if (value.isArray()) {
                // 如果是数组，合并数组中的对象
                ArrayNode mergedObject = mergedJson.has(key) ? (ArrayNode) mergedJson.get(key) : mapper.createArrayNode();
                mergeArrays(mergedObject, value, count, weight);
                mergedJson.set(key, mergedObject);
            } else {
                // 其他类型直接覆盖
                mergedJson.set(key, value);
            }
        }
    }


    private static void mergeArrays(ArrayNode mergedArray, JsonNode newArray, int count, double weight) {
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < newArray.size(); i++) {
            JsonNode newEelement = newArray.get(i);
            if (newEelement.isObject()) {
                // 如果数组元素是对象，递归合并
                ObjectNode mergedElement = (i < mergedArray.size()) ? (ObjectNode) mergedArray.get(i) : mapper.createObjectNode();
                merge(mergedElement, newEelement, count, weight);
                if (i >= mergedArray.size()) {
                    mergedArray.add(mergedElement);
                } else {
                    mergedArray.set(i, mergedElement);
                }
            } else if (newEelement.isNumber()) {
                double currentValue = (i < mergedArray.size()) ? mergedArray.get(i).asDouble() : 0.0;
                double sorce = 0;
                if (weight == 100) {
                    sorce = currentValue + newEelement.asDouble() * (weight / 100.0) / count;
                } else {
                    sorce = currentValue + newEelement.asDouble() * (weight / 100.0);
                }
                mergedArray.set(i, mapper.getNodeFactory().numberNode(MathUtils.halfUpScale2(sorce)));
            } else {
                // 其他类型，直接覆盖
                if (i >= mergedArray.size()) {
                    mergedArray.add(newEelement);
                } else {
                    mergedArray.set(i, newEelement);
                }
            }
        }
    }


    /**
     * 将JsonNode数组转为List
     *
     * @param jsonNode
     * @return
     */
    private static List<JsonNode> convertJN2List(JsonNode jsonNode) {
        List<JsonNode> result = new ArrayList<>();
        if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                result.add(node);
            }
        }
        return result;
    }


    /**
     * @return java.lang.Boolean
     * @Description 判断JsonNode 是否为自然数
     * @Author getao
     * @Date 11:27 2025/3/20
     * @Param [node]
     */
    public static Boolean isNumver(JsonNode node) {
        if (node.isInt() || node.isBigDecimal() || node.isLong() | node.isBigInteger() || node.isDouble()
                || node.isFloat()) {
            return true;
        }
        return false;
    }


    /**
     * @return java.lang.Boolean
     * @Description 判断JsonNode 是否为json对象
     * @Author getao
     * @Date 11:27 2025/3/20
     * @Param [node]
     */
    public static Boolean isJson(JsonNode node) {
        if (node.isObject() || node.isArray()) {
            return true;
        }
        return false;
    }


    /**
     * @author: getao
     * @Date: 2025/4/9 10:27
     * @Description: 统一两个json字符串的键顺序
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String sortJsonByKey(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            JsonNode sortedNode = sortNode(root);
            return mapper.writeValueAsString(sortedNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static JsonNode sortNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sortedObject = mapper.createObjectNode();
            List<String> fieldNames = new ArrayList<>();
            Iterator<String> iterator = node.fieldNames();
            while (iterator.hasNext()) {
                fieldNames.add(iterator.next());
            }
            Collections.sort(fieldNames);
            for (String fieldName : fieldNames) {
                JsonNode childNode = node.get(fieldName);
                sortedObject.set(fieldName, sortNode(childNode));
            }
            return sortedObject;
        } else if (node.isArray()) {
            ArrayNode sortedArray = mapper.createArrayNode();
            for (JsonNode element : node) {
                sortedArray.add(sortNode(element));
            }
            return sortedArray;
        }
        return node;
    }
}
