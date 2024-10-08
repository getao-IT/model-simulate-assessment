package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.entity.domain.AssessmentResultInfo;
import cn.iecas.simulate.assessment.entity.domain.IndexSystemInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.service.IndexSystemService;
import cn.iecas.simulate.assessment.util.JSONUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



/**
 * @auther getao
 * @date 2024/8/30 17:46
 * @description 指标分析工具类
 */
@Service
public class IndexAssessmentImpl {

    @Value("${assessment.fhgxfx.territoryTypeStand}")
    private int territoryTypeStand;

    @Value("${assessment.fhgxfx.territoryNumStand}")
    private int territoryNumStand;

    @Value("${assessment.fhgxfx.scandalTypeStand}")
    private int scandalTypeStand;

    @Value("${assessment.fhgxfx.scandalNumStand}")
    private int scandalNumStand;

    @Autowired
    private IndexSystemService systemService;


   /**
    * @Description 获取府会关系分析指标评估结果
    * @auther getao
    * @Date 2024/9/2 11:06
    * @Param [simulateDatas, indexSystemId, taskType]
    * @Return
    */
    public AssessmentResultInfo analysisFromFHGXFX(List<SimulateDataInfo> simulateDatas, int indexSystemId, AssessmentResultInfo resultInfo) {
        IndexSystemInfo systemInfo = this.systemService.getById(indexSystemId);
        List<String> fourIndexs = Arrays.stream(systemInfo.getFourIndex().split(",")).collect(Collectors.toList());

        //// 四级指标评估
        JSONArray four = new JSONArray();
        // 议员政策法案
        JSONObject territory = new JSONObject();
        territory.put("parentIndex", "议员政策法案分析能力");
        JSONArray territoryContents = new JSONArray();
        if (fourIndexs.contains("议员政策法案类型")) {
            JSONObject territoryType = new JSONObject();
            Set<String> territorys = simulateDatas.stream().map(SimulateDataInfo::getTerritory).collect(Collectors.toSet());
            double territoryTypeScore = new BigDecimal(territorys.size() / (double) territoryTypeStand)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            territoryType.put("name", "议员政策法案类型");
            territoryType.put("number", territorys.size() + "种");
            territoryType.put("score", (territoryTypeScore >= 1 ? 1 : territoryTypeScore) * 100);
            territoryContents.add(territoryType);
        }
        if (fourIndexs.contains("议员政策法案数量")) {
            JSONObject territoryNum = new JSONObject();
            double territoryNumScore = simulateDatas.size() / (double) territoryNumStand;
            territoryNumScore = new BigDecimal((territoryNumScore >= 1 ? 1 : territoryNumScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            territoryNum.put("name", "议员政策法案数量");
            territoryNum.put("number", simulateDatas.size() + "种");
            territoryNum.put("score", territoryNumScore);
            territoryContents.add(territoryNum);
        }
        territory.put("contents", territoryContents);
        four.add(territory);

        // 政要丑闻
        JSONObject scandal = new JSONObject();
        scandal.put("parentIndex", "元首政坛丑闻分析能力");
        JSONArray scandalContents = new JSONArray();
        if (fourIndexs.contains("政要丑闻类型")) { // 获取政要丑闻类型信息 TODO getao 28所无丑闻数据
            JSONObject scandalType = new JSONObject();
            Set<String> territorys = simulateDatas.stream().map(SimulateDataInfo::getTerritory).collect(Collectors.toSet());
            double scandalTypeScore = territorys.size() / (double) scandalTypeStand;
            scandalTypeScore = new BigDecimal((scandalTypeScore >= 1 ? 1 : scandalTypeScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            scandalType.put("name", "政要丑闻类型");
            scandalType.put("number", territorys.size() + "种");
            scandalType.put("score", scandalTypeScore);
            scandalContents.add(scandalType);
        }
        if (fourIndexs.contains("政要丑闻数量")) {
            JSONObject scandalNum = new JSONObject();
            double scandalNumScore = simulateDatas.size() / (double) scandalNumStand;
            scandalNumScore = new BigDecimal((scandalNumScore >= 1 ? 1 : scandalNumScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            scandalNum.put("name", "政要丑闻数量");
            scandalNum.put("number", simulateDatas.size() + "种");
            scandalNum.put("score", scandalNumScore);
            scandalContents.add(scandalNum);
        }
        scandal.put("contents", scandalContents);
        four.add(scandal);
        resultInfo.setFourIndex(four);

        // 三级指标评估
        JSONArray three = new JSONArray();
        JSONObject fhgxfx = new JSONObject();
        fhgxfx.put("parentIndex", "府会关系分析处理能力");
        JSONArray fhgxfxContents = new JSONArray();
        for (Object o : four) {
            JSONObject content = new JSONObject();
            JSONObject indexJson = (JSONObject) o;
            JSONArray contents = indexJson.getJSONArray("contents");
            String parentIndex = indexJson.getString("parentIndex");
            double totalScore = 0;
            for (Object content1 : contents) {
                JSONObject jsonConent = (JSONObject) content1;
                totalScore += jsonConent.getDouble("score");
            }
            if (parentIndex.contains("议员政策法案分析能力")) {
                double territoryAnalysisScore = new BigDecimal(totalScore / contents.size())
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
                content.put("name", "议员政策法案分析能力");
                content.put("score", territoryAnalysisScore);
                content.put("number", 0);
            }
            if (parentIndex.contains("元首政坛丑闻分析能力")) {
                double scandalAnalysisScore = new BigDecimal(totalScore / contents.size())
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
                content.put("name", "元首政坛丑闻分析能力");
                content.put("score", scandalAnalysisScore);
                content.put("number", 0);
            }
            fhgxfxContents.add(content);
        }
        fhgxfx.put("contents", fhgxfxContents);
        three.add(fhgxfx);
        resultInfo.setThreeIndex(three);

        // 二级指标评估
        JSONArray second = new JSONArray();
        JSONObject secondFhgxfx = new JSONObject();
        secondFhgxfx.put("parentIndex", "无");
        JSONArray secondFhgxfxContents = new JSONArray();
        for (Object o : three) {
            JSONObject content = new JSONObject();
            JSONObject indexJson = (JSONObject) o;
            JSONArray contents = indexJson.getJSONArray("contents");
            String parentIndex = indexJson.getString("parentIndex");
            double totalScore = 0;
            for (Object content1 : contents) {
                JSONObject jsonConent = (JSONObject) content1;
                totalScore += jsonConent.getDouble("score");
            }
            if (parentIndex.contains("府会关系分析处理能力")) {
               double fhynAnalysisScore = new BigDecimal(totalScore / contents.size())
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
                content.put("name", "府会关系分析处理能力");
                content.put("score", fhynAnalysisScore);
                content.put("number", 0);
            }
            secondFhgxfxContents.add(content);
        }
        secondFhgxfx.put("contents", secondFhgxfxContents);
        second.add(secondFhgxfx);
        resultInfo.setSecondIndex(second);

        // 一级指标评估
        JSONArray firstIndex = new JSONArray();
        JSONObject usability = new JSONObject();
        usability.put("name", "可用性");
        usability.put("value", "可用");
        JSONObject realTime = new JSONObject();
        realTime.put("name", "数据实时性");
        realTime.put("value", "实时传输");
        JSONObject conformity = new JSONObject();
        conformity.put("name", "接口符合性");
        conformity.put("value", "符合");
        JSONObject normative = new JSONObject();
        normative.put("name", "格式规范性");
        normative.put("value", "规范");
        firstIndex.add(usability);
        firstIndex.add(realTime);
        firstIndex.add(conformity);
        firstIndex.add(normative);
        resultInfo.setFirstIndex(firstIndex);

        // 综合评分
        double secondAvg = this.getIndexAvg(second);
        secondAvg = Double.isNaN(secondAvg) ? 0.0 : secondAvg;
        double threeAvg = this.getIndexAvg(three);
        threeAvg = Double.isNaN(threeAvg) ? 0.0 : threeAvg;
        double fourAvg = this.getIndexAvg(four);
        fourAvg = Double.isNaN(fourAvg) ? 0.0 : fourAvg;
        double overallScore = new BigDecimal((secondAvg + threeAvg + fourAvg) / 3).setScale(2, RoundingMode.HALF_UP).doubleValue();
        resultInfo.setScore(overallScore);

        return resultInfo;
    }

    private double getIndexAvg(JSONArray index) {
        double totalScore = 0;
        int number = 0;
        for (Object o : index) {
            JSONObject jsonIndex = (JSONObject) o;
            JSONArray contents = jsonIndex.getJSONArray("contents");
            for (Object content1 : contents) {
                JSONObject jsonConent = (JSONObject) content1;
                totalScore += jsonConent.getDouble("score");
            }
            number += contents.size();
        }
        return totalScore / number;
    }
}
