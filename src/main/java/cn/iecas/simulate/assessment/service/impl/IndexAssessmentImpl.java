package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.entity.domain.AssessmentResultInfo;
import cn.iecas.simulate.assessment.entity.domain.IndexSystemInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.service.IndexSystemService;
import cn.iecas.simulate.assessment.util.JSONUtils;
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
        List<String> firstIndexs = Arrays.stream(systemInfo.getFirstIndex().split(",")).collect(Collectors.toList());
        List<String> secondIndexs = Arrays.stream(systemInfo.getSecondIndex().split(",")).collect(Collectors.toList());
        List<String> threeIndexs = Arrays.stream(systemInfo.getThreeIndex().split(",")).collect(Collectors.toList());
        List<String> fourIndexs = Arrays.stream(systemInfo.getFourIndex().split(",")).collect(Collectors.toList());

        // 四级指标评估
        JSONObject four = new JSONObject();
        if (fourIndexs.contains("议员政策法案类型")) {
            Set<String> territorys = simulateDatas.stream().map(SimulateDataInfo::getTerritory).collect(Collectors.toSet());
            double territoryTypeScore = new BigDecimal(territorys.size() / (double) territoryTypeStand)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            four.put("议员政策法案类型", territorys.size() + "种");
            four.put("议员政策法案类型得分", (territoryTypeScore >= 1 ? 1 : territoryTypeScore) * 100);
        }
        if (fourIndexs.contains("议员政策法案数量")) {
            double territoryNumScore = simulateDatas.size() / (double) territoryNumStand;
            territoryNumScore = new BigDecimal((territoryNumScore >= 1 ? 1 : territoryNumScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            four.put("议员政策法案数量", simulateDatas.size() + "种");
            four.put("议员政策法案数量得分", territoryNumScore);
        }
        if (fourIndexs.contains("政要丑闻类型")) { // 获取政要丑闻类型信息 TODO getao 28所无丑闻数据
            Set<String> territorys = simulateDatas.stream().map(SimulateDataInfo::getTerritory).collect(Collectors.toSet());
            double scandalTypeScore = territorys.size() / (double) scandalTypeStand;
            scandalTypeScore = new BigDecimal((scandalTypeScore >= 1 ? 1 : scandalTypeScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            four.put("政要丑闻类型", territorys.size() + "种");
            four.put("政要丑闻类型得分", scandalTypeScore);
        }
        if (fourIndexs.contains("政要丑闻数量")) {
            double scandalNumScore = simulateDatas.size() / (double) scandalNumStand;
            scandalNumScore = new BigDecimal((scandalNumScore >= 1 ? 1 : scandalNumScore) * 100)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            four.put("政要丑闻数量", simulateDatas.size() + "种");
            four.put("政要丑闻数量得分", scandalNumScore);
        }
        resultInfo.setFourIndex(four);

        // 三级指标评估
        JSONObject three = new JSONObject();
        if (threeIndexs.contains("议员政策法案分析能力")) {
            double territoryScore = four.get("议员政策法案类型得分") != null ? four.getDouble("议员政策法案类型得分") : 0.0;
            double territoryNumScore = four.get("议员政策法案数量得分") != null ? four.getDouble("议员政策法案数量得分") : 0.0;
            double territoryAnalysisScore = new BigDecimal((territoryScore + territoryNumScore) / 2)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            three.put("议员政策法案分析能力", territoryAnalysisScore);
        }
        if (threeIndexs.contains("元首政坛丑闻分析能力")) {
            double scandalScore = four.get("政要丑闻类型得分") != null ? four.getDouble("政要丑闻类型得分") : 0.0;
            double scandalNumScore = four.get("政要丑闻数量得分") != null ? four.getDouble("政要丑闻数量得分") : 0.0;
            double scandalAnalysisScore = new BigDecimal((scandalScore + scandalNumScore) / 2)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            three.put("元首政坛丑闻分析能力", scandalAnalysisScore);
        }
        resultInfo.setThreeIndex(three);

        // 二级指标评估
        JSONObject second = new JSONObject();
        if (secondIndexs.contains("府会关系分析处理能力")) {
            double territoryAnalysisScore = three.get("议员政策法案分析能力") != null ? three.getDouble("议员政策法案分析能力") : 0.0;
            double scandalAnalysisScore = three.get("元首政坛丑闻分析能力") != null ? three.getDouble("元首政坛丑闻分析能力") : 0.0;
            double fhynAnalysisScore = new BigDecimal((territoryAnalysisScore + scandalAnalysisScore) / 2)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            second.put("府会议案分析处理能力", fhynAnalysisScore);
        }
        resultInfo.setSecondIndex(second);

        // 一级指标评估
        JSONObject first = new JSONObject();
        first.put("可用性", "可用");
        first.put("数据实时性", "实时传输");
        first.put("接口符合性", "符合");
        first.put("格式规范性", "规范");
        resultInfo.setFirstIndex(first);

        // 综合评分
        JSONObject secondSum = JSONUtils.getJsonValueSum(second, new JSONObject());
        double secondAvg = (secondSum.getDouble("sum") / secondSum.getInteger("count"));
        secondAvg = Double.isNaN(secondAvg) ? 0.0 : secondAvg;
        JSONObject threeSum = JSONUtils.getJsonValueSum(three, new JSONObject());
        double threeAvg = (threeSum.getDouble("sum") / threeSum.getInteger("count"));
        threeAvg = Double.isNaN(threeAvg) ? 0.0 : threeAvg;
        JSONObject fourSum = JSONUtils.getJsonValueSum(four, new JSONObject());
        double fourAvg = (fourSum.getDouble("sum") / fourSum.getInteger("count"));
        fourAvg = Double.isNaN(fourAvg) ? 0.0 : fourAvg;
        double overallScore = new BigDecimal((secondAvg + threeAvg + fourAvg) / 3).setScale(2, RoundingMode.HALF_UP).doubleValue();
        resultInfo.setScore(overallScore);

        return resultInfo;
    }
}
