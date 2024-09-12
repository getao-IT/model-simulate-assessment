package cn.iecas.simulate.assessment.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.json.JSONException;
import java.util.*;



public class FormatJson {


    public static String vifToJson(org.json.JSONObject jsonObject) throws JSONException {
        org.json.JSONObject object = new org.json.JSONObject();
        org.json.JSONArray jsonArray = new org.json.JSONArray();

        if ((jsonObject.getJSONObject("判读标注")).getJSONObject("Children").get("Child") instanceof org.json.JSONObject){
            jsonArray.put((jsonObject.getJSONObject("判读标注")).getJSONObject("Children").get("Child"));
        }
        else {
            jsonArray = (jsonObject.getJSONObject("判读标注")).getJSONObject("Children").getJSONArray("Child");
        }
        for (int i = 0; i< jsonArray.length(); i++){
            org.json.JSONObject jsonObj = (org.json.JSONObject) jsonArray.get(i);
            jsonObj.put("description","经纬度坐标");
            jsonObj.put("coordinate","geodegree");
        }
        object.put("objects",jsonArray);

        jsonObject.getJSONObject("判读标注").put("Children",object);
        Map<String,String> keyMap = new HashMap<>();

        keyMap.put("判读标注","annotation");
        keyMap.put("Child","objects");
        keyMap.put("GeoShape","points");
        keyMap.put("GeoShapePoint","point");
        org.json.JSONObject jsonObjects = changeJsonObj(jsonObject,keyMap);
        org.json.JSONObject annotation = jsonObjects.getJSONObject("annotation");
        org.json.JSONArray objects = annotation.getJSONObject("Children").getJSONArray("objects");
        for (int i = 0 ; i< objects.length() ; i++){
            org.json.JSONObject o = objects.getJSONObject(i);
            org.json.JSONArray points = o.getJSONObject("points").getJSONArray("point");
            List<org.json.JSONObject> possibleresult = new ArrayList<>();
            org.json.JSONObject possible = new org.json.JSONObject();
            possible.put("name",o.get("name").toString());
            //System.out.println(o.getString("name"));
            possible.put("probability",1);
            possibleresult.add(possible);
            List<String> pointList = new ArrayList<>();
            for (int index = 0; index < points.length(); index++) {
                org.json.JSONObject point = points.getJSONObject(index);
                double x = point.getDouble("x");
                double y = point.getDouble("y");
                double[] coordinates = new double[]{x, y};
                String po = coordinates[0] + "," + coordinates[1];
                pointList.add(po);
            }
            o.put("points",pointList);
            o.put("possibleresult",possibleresult);
            o.remove("name");
        }
        org.json.JSONArray json = annotation.getJSONObject("Children").getJSONArray("objects");
        annotation.put("objects",json);
        annotation.remove("Children");
        org.json.JSONObject source = new org.json.JSONObject();
        source.put("filename",annotation.getString("imageSource").substring(annotation.getString("imageSource").lastIndexOf("\\")+1));
        annotation.put("source",source);
        annotation.remove("imageSource");
        return jsonObjects.toString();
    }


    public static org.json.JSONObject jsonToVif(String labelInfo){
        org.json.JSONObject jsonObject = new org.json.JSONObject(labelInfo);
        org.json.JSONArray jsonArray = jsonObject.getJSONArray("object");
        List<org.json.JSONObject> objects = new ArrayList<>();
        for (Object object : jsonArray){

            org.json.JSONObject perObject = new org.json.JSONObject(object.toString());
            org.json.JSONObject pointObjects = perObject.getJSONObject("points");
            org.json.JSONArray points = pointObjects.getJSONArray("point");
            List<org.json.JSONObject> pointList = new ArrayList<>();
            for (Object point : points){
                List<String> coorpoint = Arrays.asList(point.toString().split(","));
                org.json.JSONObject pointObject = new org.json.JSONObject();
                pointObject.put("x",coorpoint.get(0));
                pointObject.put("y",coorpoint.get(1));
                pointList.add(pointObject);
            }
            pointObjects.put("GeoShapePoint",pointList).remove("point");
            perObject.put("GeoShape",pointObjects).remove("points");
            objects.add(perObject);
        }
        jsonObject.put("Child",objects).remove("object");


        org.json.JSONObject childrenObject = new org.json.JSONObject();
        org.json.JSONObject headObject = new org.json.JSONObject();
        childrenObject.put("Childs",jsonObject);
        headObject.put("判读标注",childrenObject);
        return headObject;
    }


    public static org.json.JSONObject changeJsonObj(org.json.JSONObject jsonObject, Map<String, String> keyMap) throws JSONException {
        org.json.JSONObject readJson = new org.json.JSONObject();
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet){
            String finalKey = keyMap.get(key) == null ? key : keyMap.get(key);
            try {

                org.json.JSONObject jsonObject1 = jsonObject.getJSONObject(key);
                readJson.put(finalKey,changeJsonObj(jsonObject1,keyMap));
            }catch (Exception e){
                try {
                    org.json.JSONArray jsonArray = jsonObject.getJSONArray(key);
                    readJson.put(finalKey,changeJsonArr(jsonArray,keyMap));
                }catch (Exception x){
                    readJson.put(finalKey,jsonObject.get(key));
                }
            }

        }
        return readJson;
    }


    public static org.json.JSONArray changeJsonArr(org.json.JSONArray jsonArray, Map<String, String> keyMap) throws JSONException {
        org.json.JSONArray readJson = new org.json.JSONArray();
        for (int i = 0; i < jsonArray.length() ; i++){
            org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
            readJson.put(changeJsonObj(jsonObject,keyMap));
        }
        return readJson;
    }


    //json小改动
    public String possibleResultToArray(String jsonStr){
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        try {
            JSONArray jsonArray = ((jsonObject.getJSONObject("annotation")).getJSONObject("objects")).getJSONArray("object");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = (JSONObject) jsonArray.get(i);
                if (!object.get("possibleresult").getClass().getName().equals("net.sf.json.JSONArray")){
                    object.put("possibleresult",object.getJSONObject("possibleresult"));
                    JSONArray jsonArr = new JSONArray();
                    jsonArr.add(object.getJSONObject("possibleresult"));
                    object.put("possibleresult", jsonArr);
                }
                else{
                    object.put("possibleresult",object.getJSONArray("possibleresult"));
                }

            }
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("objects", jsonArray);
            jsonObj.put("source", jsonObject.getJSONObject("annotation").get("source"));
            jsonObject.put("annotation", jsonObj);
            return jsonObject.toString();
        }catch (Exception e){
            return null;
        }
    }
}
