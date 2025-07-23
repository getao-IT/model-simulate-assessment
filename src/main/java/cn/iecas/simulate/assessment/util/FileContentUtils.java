package cn.iecas.simulate.assessment.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileContentUtils {

    public static Map<String, Object> compareFiles(Path file1, Path file2) throws IOException {
        // 读取文件并记录行号和内容
        Map<LineContent, List<Integer>> file1Content = readFileWithLineNumbers(file1);
        Map<LineContent, List<Integer>> file2Content = readFileWithLineNumbers(file2);

        // 找出只存在于文件1的内容
        List<Map<String, Object>> uniqueToFile1 = new ArrayList<>();
        // 找出只存在于文件2的内容
        List<Map<String, Object>> uniqueToFile2 = new ArrayList<>();

        // 比较两个文件的内容
        compareContentFrequencies(file1Content, file2Content, uniqueToFile1, uniqueToFile2);
        compareContentFrequencies(file2Content, file1Content, uniqueToFile2, uniqueToFile1);

        // 按行号排序结果
        sortResultsByLineNumber(uniqueToFile1);
        sortResultsByLineNumber(uniqueToFile2);

        // 统计信息
        Map<String, Integer> statistics = new LinkedHashMap<>();
        int totalLines1 = file1Content.values().stream().mapToInt(List::size).sum();
        int totalLines2 = file2Content.values().stream().mapToInt(List::size).sum();
        statistics.put("file1TotalLines", totalLines1);
        statistics.put("file2TotalLines", totalLines2);
        statistics.put("uniqueToFile1Count", uniqueToFile1.size());
        statistics.put("uniqueToFile2Count", uniqueToFile2.size());

        // 构建最终结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("uniqueToFile1", uniqueToFile1);
        result.put("uniqueToFile2", uniqueToFile2);
        result.put("statistics", statistics);

        return result;
    }

    private static void compareContentFrequencies(
            Map<LineContent, List<Integer>> sourceMap,
            Map<LineContent, List<Integer>> targetMap,
            List<Map<String, Object>> sourceUnique,
            List<Map<String, Object>> targetUnique) {

        // 检查源内容在目标中的出现情况
        for (Map.Entry<LineContent, List<Integer>> entry : sourceMap.entrySet()) {
            LineContent content = entry.getKey();
            List<Integer> sourceLines = entry.getValue();

            // 在目标文件中查找相同内容
            List<Integer> targetLines = targetMap.getOrDefault(content, Collections.emptyList());

            // 计算差异数量
            int diffCount = sourceLines.size() - targetLines.size();

            // 如果源文件中该内容出现次数更多
            if (diffCount > 0) {
                // 取前diffCount个行号作为差异
                for (int i = 0; i < diffCount; i++) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("content", content.content);
                    item.put("lineNumber", sourceLines.get(i));
                    sourceUnique.add(item);
                }
            }
        }
    }

    private static Map<LineContent, List<Integer>> readFileWithLineNumbers(Path file) throws IOException {
        Map<LineContent, List<Integer>> contentMap = new HashMap<>();
        List<String> lines = Files.readAllLines(file);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                LineContent key = new LineContent(line);
                contentMap.computeIfAbsent(key, k -> new ArrayList<>()).add(i + 1);
            }
        }
        return contentMap;
    }

    private static void sortResultsByLineNumber(List<Map<String, Object>> results) {
        results.sort(Comparator.comparingInt(m -> (Integer) m.get("lineNumber")));
    }

    // 自定义内容类，用于精确比较行内容
    static class LineContent {
        final String content;

        LineContent(String content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LineContent that = (LineContent) o;
            return content.equals(that.content);
        }

        @Override
        public int hashCode() {
            return content.hashCode();
        }
    }

    public static void main(String[] args) throws IOException {
        // 示例用法
        Path file1 = Paths.get("D:\\iecas\\temp\\sample.txt");
        Path file2 = Paths.get("D:\\iecas\\temp\\oupu.txt");

        Map<String, Object> result = compareFiles(file1, file2);

        // 转换为JSON字符串（实际使用时可用Jackson/Gson等库）
        String json = toJsonString(result);
        System.out.println(json);
    }

    // 简单JSON转换方法（生产环境建议使用Jackson/Gson）
    private static String toJsonString(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");

            if (entry.getValue() instanceof List) {
                json.append(listToJson((List<?>) entry.getValue()));
            } else if (entry.getValue() instanceof Map) {
                json.append(mapToJson((Map<?, ?>) entry.getValue()));
            } else {
                json.append(entry.getValue());
            }

            json.append(",");
        }
        if (!map.isEmpty()) json.deleteCharAt(json.length() - 1);
        json.append("}");
        return json.toString();
    }

    private static String listToJson(List<?> list) {
        StringBuilder json = new StringBuilder("[");
        for (Object item : list) {
            if (item instanceof Map) {
                json.append(mapToJson((Map<?, ?>) item));
            } else {
                json.append("\"").append(item).append("\"");
            }
            json.append(",");
        }
        if (!list.isEmpty()) json.deleteCharAt(json.length() - 1);
        json.append("]");
        return json.toString();
    }

    private static String mapToJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");

            if (entry.getValue() instanceof List) {
                json.append(listToJson((List<?>) entry.getValue()));
            } else if (entry.getValue() instanceof Number) {
                json.append(entry.getValue());
            } else {
                json.append("\"").append(entry.getValue()).append("\"");
            }

            json.append(",");
        }
        if (!map.isEmpty()) json.deleteCharAt(json.length() - 1);
        json.append("}");
        return json.toString();
    }


}