package cn.iecas.simulate.assessment.util;

import com.alibaba.fastjson.JSONObject;
import java.util.*;
import java.util.stream.Collectors;



/**
 * Collections工具类
 */
public class CollectionsUtils {

    public static final String SORT_ASC = "ASC";

    public static final String SORT_DESC = "DESC";


    /**
     * 返回根据key排序的Map集合， 默认倒序排列
     * @param map
     * @param sortord 排序方式
     * @return
     */
    public static <K, V> Map<K, V> sortMapByNumKey(Map<K, V> map, String sortord) {
        if (map == null || map.isEmpty()) {
            throw new NullPointerException("对象 map 的值不能为空");
        }
        Map<K, V> treeMap = new TreeMap<>(new Comparator<K>() {
            @Override
            public int compare(K o1, K o2) {
                if (sortord.equalsIgnoreCase(SORT_ASC))
                    return o1.hashCode() - o2.hashCode();
                if (sortord.equalsIgnoreCase(SORT_DESC))
                    return o2.hashCode() - o1.hashCode();
                return String.valueOf(o2).compareTo(String.valueOf(o1));
            }
        });
        treeMap.putAll(map);
        return treeMap;
    }


    /**
     * 返回根据key排序的Map集合， 默认倒序排列
     * @param map
     * @param sortord 排序方式
     * @return
     */
    public static <K, V> Map<K, V> sortMapByStrKey(Map<K, V> map, String sortord) {
        if (map == null || map.isEmpty()) {
            throw new NullPointerException("对象 map 的值不能为空");
        }
        Map<K, V> treeMap = new TreeMap<>(new Comparator<K>() {
            @Override
            public int compare(K o1, K o2) {
                if (sortord.equalsIgnoreCase(SORT_ASC))
                    return o1.hashCode() - o2.hashCode();
                if (sortord.equalsIgnoreCase(SORT_DESC))
                    return o2.hashCode() - o1.hashCode();
                return String.valueOf(o2).compareTo(String.valueOf(o1));
            }
        });
        treeMap.putAll(map);
        return treeMap;
    }


    /**
     * 返回根据VALUE排序的Map集合， 默认倒序排列
     * @param map
     * @param sortord
     * @return
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, String sortord) {
        if (map == null || map.isEmpty()) {
            throw new NullPointerException("对象 map 的值不能为空");
        }
        Map<K, V> result = new LinkedHashMap<>();
        ArrayList<Map.Entry<K, V>> entries = new ArrayList<>(map.entrySet());
        entries.sort(new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                if (sortord.equalsIgnoreCase(SORT_ASC))
                    return String.valueOf(o1.getValue()).compareTo(String.valueOf(o2.getValue()));
                if (sortord.equalsIgnoreCase(SORT_DESC))
                    return String.valueOf(o2.getValue()).compareTo(String.valueOf(o1.getValue()));
                return String.valueOf(o2.getValue()).compareTo(String.valueOf(o1.getValue()));
            }
        });
        entries.forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }


    /**
     * 将Map转换为JSONObject
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> JSONObject parseMapToJsonobject(Map<K, V> map) {
        return new JSONObject((Map<String, Object>) map);
    }


    /**
     * 将Map转换为List
     * @param map
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> List<Map.Entry<K, V>> parseMapToList(Map<K, V> map) {
        return new ArrayList<>(map.entrySet());
    }


   /**
    * @Description 将给定集合按照权重比例返回
    * @auther getao
    * @Date 2024/9/4 16:03
    * @Param [listDatas, weight]
    * @Return java.util.List<T>
    */
    public static <T> List<T> getListByWeight(List<T> listDatas, int weight) {
        int size = listDatas.size();
        int targetSize = (int) Math.round(size * (weight / 100.0));
        Set<T> tempSet = new HashSet<>();
        do {
            tempSet.add(listDatas.get(new Random().nextInt(size)));
        } while (tempSet.size() < targetSize);
        List<T> targetList = tempSet.stream().collect(Collectors.toList());
        return targetList;
    }


    public static void main(String[] args) {
        Map<Object, String> map = new HashMap<>();
        map.put(89, "12");
        map.put(1, "123");
        map.put(52, "50");
        map.put("ss", "56");
        map.put("ad", "25");
        map.put("12adf", "21");
        map.put(10, "11");
        map.put(58, "5566");
        System.out.println(map);
        System.out.println("-----------------==============-------------------");
        map = CollectionsUtils.sortMapByValue(map, "ASC");
        System.out.println(map);

        // FINISHED,TRANSFERRING,WAITING,FAILED
        /*System.out.println("FINISHED".hashCode());
        System.out.println("TRANSFERRING".hashCode());
        System.out.println("WAITING".hashCode());
        System.out.println("FAILED".hashCode());
        Map<Object, String> map = new HashMap<>();
        map.put(89, "FINISHED");
        map.put(52, "WAITING");
        map.put(1, "TRANSFERRING");
        map.put("ss", "FAILED");
        System.out.println(map);
        System.out.println("-----------------==============-------------------");
        map = CollectionsUtils.sortMapByValue(map, "DESC");
        System.out.println(map);*/
    }
}
