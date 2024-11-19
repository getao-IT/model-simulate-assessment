package cn.iecas.simulate.assessment.service.model.impl;

import cn.iecas.simulate.assessment.entity.domain.*;
import cn.iecas.simulate.assessment.entity.model.domain.IndexIndicatorTaskInfo;
import cn.iecas.simulate.assessment.entity.model.domain.ZbCompareInfo;
import cn.iecas.simulate.assessment.service.AssessmentProcessService;
import cn.iecas.simulate.assessment.service.IndexInfoService;
import cn.iecas.simulate.assessment.service.IndexSystemService;
import cn.iecas.simulate.assessment.service.model.AssessmentService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @auther getao
 * @date 2024/8/30 11:04
 * @description 中美对比模型仿真数据分析评估服务实现类
 */
@Service(value = "ZMDB-ASMTSERVICE")
public class AssessmentZMDBServiceImpl implements AssessmentService<IndexIndicatorTaskInfo> {

    @Value("${assessment.zmdb.indexBuildType}")
    private int indexBuildType;

    @Value("${assessment.zmdb.indexBuildNum}")
    private int indexBuildNum;

    @Value("${assessment.zmdb.overallCompareType}")
    private int overallCompareType;

    @Value("${assessment.zmdb.overallCompareNum}")
    private int overallCompareNum;

    @Value("${assessment.zmdb.economyType}")
    private int economyType;

    @Value("${assessment.zmdb.economyNum}")
    private int economyNum;

    @Value("${assessment.zmdb.militaryType}")
    private int militaryType;

    @Value("${assessment.zmdb.militaryNum}")
    private int militaryNum;

    @Value("${assessment.zmdb.technologyType}")
    private int technologyType;

    @Value("${assessment.zmdb.technologyNum}")
    private int technologyNum;

    @Autowired
    private IndexSystemService systemService;

    @Autowired
    private IndexInfoService infoService;

    @Autowired
    private AssessmentProcessService processService;


   /**
    * @Description 获取府会关系分析模型评估结果
    * @auther getao
    * @Date 2024/8/30 11:15
    * @Param [indexSystemId]
    * @Return
    */
    @Override
    public AssessmentResultInfo getModelAssessmentInfo(List<IndexIndicatorTaskInfo> simulateDatas, int indexSystemId, AssessmentResultInfo resultInfo) {
        if (simulateDatas != null && simulateDatas.size() != 0) {
            resultInfo = this.analysisFromZMDB(simulateDatas, indexSystemId, resultInfo);
        } else {
            JSONArray firstIndex = new JSONArray();
            JSONObject usability = new JSONObject();
            usability.put("name", "可用性");
            usability.put("value", "不可用");
            JSONObject realTime = new JSONObject();
            realTime.put("name", "数据实时性");
            realTime.put("value", "无数据");
            JSONObject conformity = new JSONObject();
            conformity.put("name", "接口符合性");
            conformity.put("value", "不符合");
            JSONObject normative = new JSONObject();
            normative.put("name", "格式规范性");
            normative.put("value", "不规范");
            firstIndex.add(usability);
            firstIndex.add(realTime);
            firstIndex.add(conformity);
            firstIndex.add(normative);
            resultInfo.setFirstIndex(firstIndex);
            resultInfo.setScore(0.0);
            resultInfo.setContibution(0.0);
        }

        return resultInfo;
    }


    /**
     *  @author: getao
     *  @Date: 2024/10/12 10:44
     *  @Description: 获取模型仿真评估结果
     */
    @Transactional
    public AssessmentResultInfo analysisFromZMDB(List<IndexIndicatorTaskInfo> simulateDatas, int indexSystemId, AssessmentResultInfo resultInfo) {
        IndexSystemInfo indexSystemInfo = this.systemService.getById(indexSystemId);
        int modelId = indexSystemInfo.getModelId();
        int batchNo = indexSystemInfo.getBatchNo();

        QueryWrapper<IndexInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("model_id", modelId).eq("batch_no", batchNo);
        List<IndexInfo> indexInfos = this.infoService.getIndexInfoByQuery(queryWrapper);

        String assessmentUuid = UUID.randomUUID().toString();
        // 四级指标评估
        Map<Integer, List<IndexInfo>> fourIndexInfo = indexInfos.stream().filter(e -> e.getLevel() == 4).collect(Collectors.groupingBy(e -> e.getParentIndexId()));
        this.getIndexAssessmentByLevel(resultInfo, 4, assessmentUuid, fourIndexInfo, simulateDatas);

        // 三级指标评估
        Map<Integer, List<IndexInfo>> thireIndexInfo = indexInfos.stream().filter(e -> e.getLevel() == 3).collect(Collectors.groupingBy(e -> e.getParentIndexId()));
        this.getIndexAssessmentByLevel(resultInfo, 3, assessmentUuid, thireIndexInfo, null);

        // 二级指标评估
        Map<Integer, List<IndexInfo>> secondIndexInfo = indexInfos.stream().filter(e -> e.getLevel() == 2).collect(Collectors.groupingBy(e -> e.getParentIndexId()));
        this.getIndexAssessmentByLevel(resultInfo, 2, assessmentUuid, secondIndexInfo, null);

        // 一级指标评估
        this.getFirstIndexAssessmentResult(resultInfo);

        // 综合评分
        this.getOverallScore(resultInfo);

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


    /**
     *  获取综合评分
     */
    private void getOverallScore(AssessmentResultInfo resultInfo) {
        double secondAvg = this.getIndexAvg(resultInfo.getSecondIndex());
        secondAvg = Double.isNaN(secondAvg) ? 0.0 : secondAvg;
        double threeAvg = this.getIndexAvg(resultInfo.getThreeIndex());
        threeAvg = Double.isNaN(threeAvg) ? 0.0 : threeAvg;
        double fourAvg = this.getIndexAvg(resultInfo.getFourIndex());
        fourAvg = Double.isNaN(fourAvg) ? 0.0 : fourAvg;
        double overallScore = new BigDecimal((secondAvg + threeAvg + fourAvg) / 3).setScale(2, RoundingMode.HALF_UP).doubleValue();
        resultInfo.setScore(overallScore);
    }


    /**
     *  获取一级指标评估结果
     */
    private void getFirstIndexAssessmentResult(AssessmentResultInfo resultInfo) {
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
    }


    /**
     * 获取某一级别指标评估结果
     */
    private void getIndexAssessmentByLevel(AssessmentResultInfo resultInfo, int indexLevel, String assessmentUuid,
                                           Map<Integer, List<IndexInfo>> indexInfos, List<IndexIndicatorTaskInfo> simulateDatas) {
        JSONArray result = new JSONArray();
        Set<Integer> foutKeySet = indexInfos.keySet();
        for (Integer parentIndexId : foutKeySet) {
            JSONObject elemnt = new JSONObject();
            IndexInfo parentIndexInfo = this.infoService.getIndexInfoById(parentIndexId);
            String parentIndexName = parentIndexInfo == null ? "无" : parentIndexInfo.getIndexName();
            elemnt.put("parentIndex", parentIndexName);
            List<IndexInfo> contentIndexs = indexInfos.get(parentIndexId);
            JSONArray contents = new JSONArray();
            List<AssessmentProcessInfo> processs = new ArrayList<>();
            for (IndexInfo indexInfo : contentIndexs) {
                double score = 0;
                if (indexInfo.getIndexName().contains("体系贡献率")) {
                    continue;
                } else if (indexLevel == 4) {
                    score = this.buildSingleFourIndexAssessment(indexInfo, simulateDatas, contents);
                } else {
                    score = this.buildSingleIndexAssessment(assessmentUuid, indexInfo, contents);
                }
                AssessmentProcessInfo processInfo = AssessmentProcessInfo.builder().indexId(indexInfo.getId())
                        .modelId(indexInfo.getModelId()).batchNo(indexInfo.getBatchNo()).parentIndexId(parentIndexId)
                        .sourceIndexId(indexInfo.getSourceIndexId()).assessmentUuid(assessmentUuid).result(score).build();
                processs.add(processInfo);
            }
            boolean b = this.processService.batchInsert(processs);
            elemnt.put("contents", contents);
            result.add(elemnt);
        }
        if (indexLevel == 4) {
            resultInfo.setFourIndex(result);
        } else if (indexLevel == 3) {
            resultInfo.setThreeIndex(result);
        } else if (indexLevel == 2) {
            resultInfo.setSecondIndex(result);
        }
    }


    /**
     *  构建三级、二级单个指标评估结果
     */
    private double buildSingleIndexAssessment(String assessmentUuid, IndexInfo indexInfo, JSONArray contents) {
        QueryWrapper<AssessmentProcessInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("assessment_uuid", assessmentUuid).eq("model_id", indexInfo.getModelId())
                .eq("parent_index_id", indexInfo.getId());
        List<AssessmentProcessInfo> processInfos = this.processService.listByQueryWrapper(wrapper);
        double score = 0;
        if (processInfos.size() != 0) {
            double totalScore = processInfos.stream().mapToDouble(AssessmentProcessInfo::getResult).sum();
            score = new BigDecimal(totalScore / processInfos.size())
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        JSONObject element = new JSONObject();
        element.put("name", indexInfo.getIndexName());
        element.put("score", score);
        element.put("number", 0);
        contents.add(element);
        return score;
    }


    /**
     *  构建四级单个指标评估结果，此接口逻辑严格根据四级指标名称进行定位
     */
    private double buildSingleFourIndexAssessment(IndexInfo indexInfo, List<IndexIndicatorTaskInfo> assessmentDatas, JSONArray contents) {
        double score = 0;
        if (indexInfo.getIndexName().equalsIgnoreCase("指标构建类型")) {
            JSONObject type = new JSONObject();
            int size = assessmentDatas.size();
            score = new BigDecimal(size / (double) indexBuildType)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            score = (score >= 1 ? 1 : score) * 100;
            type.put("name", indexInfo.getIndexName());
            type.put("number", size + "种");
            type.put("score", score);
            contents.add(type);
        } else if (indexInfo.getIndexName().equalsIgnoreCase("指标构建数量")) {
            JSONObject type = new JSONObject();
            int size = 0;
            for (IndexIndicatorTaskInfo assessmentData : assessmentDatas) {
                size += (1 + assessmentData.getCompareInfos().size());
            }
            score = new BigDecimal(size / (double) indexBuildNum)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            score = (score >= 1 ? 1 : score) * 100;
            type.put("name", indexInfo.getIndexName());
            type.put("number", size + "条");
            type.put("score", score);
            contents.add(type);
        } else {
            JSONObject element = new JSONObject();
            int stand = 0;
            int size = 0;

            if (indexInfo.getIndexName().equalsIgnoreCase("综合对比维度类型")) {
                size = this.getIndexTypeNum(assessmentDatas, "综合实力");
                stand = overallCompareType;
            }
            if (indexInfo.getIndexName().equalsIgnoreCase("经济力量对比维度类型")) {
                size = this.getIndexTypeNum(assessmentDatas, "经济力量");
                stand = economyType;
            }
            if (indexInfo.getIndexName().equalsIgnoreCase("军事力量对比维度类型")) {
                size = this.getIndexTypeNum(assessmentDatas, "军力实力");
                stand = militaryType;
            }
            if (indexInfo.getIndexName().equalsIgnoreCase("科技实力对比维度类型")) {
                size = this.getIndexTypeNum(assessmentDatas, "科技实力");
                stand = technologyType;
            }

            if (indexInfo.getIndexName().equalsIgnoreCase("综合对比指标数量")) {
                size = this.getIndexNumNum(assessmentDatas, "综合实力");
                stand = overallCompareNum;
            }
            if (indexInfo.getIndexName().equalsIgnoreCase("经济力量对比指标数量")) {
                size = this.getIndexTypeNum(assessmentDatas, "经济力量");
                stand = economyNum;
            }
            if (indexInfo.getIndexName().equalsIgnoreCase("军事力量对比指标数量")) {
                size = this.getIndexTypeNum(assessmentDatas, "军力实力");
                stand = militaryNum;
            }
            if (indexInfo.getIndexName().equalsIgnoreCase("科技实力对比指标数量")) {
                size = this.getIndexTypeNum(assessmentDatas, "科技实力");
                stand = technologyNum;
            }
            score = new BigDecimal(size / (double) stand)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue();
            score = (score >= 1 ? 1 : score) * 100;
            element.put("name", indexInfo.getIndexName());
            element.put("number", size);
            element.put("score", score);
            contents.add(element);
        }

        return score;
    }


    private int getIndexTypeNum(List<IndexIndicatorTaskInfo> assessmentDatas, String taskName) {
        int sum = assessmentDatas.stream().filter(e -> e.getTaskname().contains(taskName))
                .map(e -> e.getCompareInfos()).mapToInt(e -> e.stream().mapToInt(f -> f.getChildren().size()).sum()).sum();
        return sum;
    }


    private int getIndexNumNum(List<IndexIndicatorTaskInfo> assessmentDatas, String taskName) {
        int sum = assessmentDatas.stream().filter(e -> e.getTaskname().contains(taskName))
                .map(e -> e.getCompareInfos()).mapToInt(e -> e.stream().mapToInt(f -> f.getChildren().toJavaList(ZbCompareInfo.class)
                .stream().mapToInt(g->g.getChildren().size()).sum()).sum()).sum();
        return sum;
    }
}
