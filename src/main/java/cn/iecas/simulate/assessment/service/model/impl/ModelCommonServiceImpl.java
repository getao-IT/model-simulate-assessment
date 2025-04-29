package cn.iecas.simulate.assessment.service.model.impl;

import cn.iecas.simulate.assessment.dao.AssessmentStatisticDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import cn.iecas.simulate.assessment.entity.domain.IndexResultInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.domain.ModelInfo;
import cn.iecas.simulate.assessment.entity.model.emun.AssessmentType;
import cn.iecas.simulate.assessment.entity.model.emun.ModelDataType;
import cn.iecas.simulate.assessment.entity.model.emun.ModelType;
import cn.iecas.simulate.assessment.service.ModelService;
import cn.iecas.simulate.assessment.service.model.AssessmentService;
import cn.iecas.simulate.assessment.service.model.ModelTypeService;
import cn.iecas.simulate.assessment.service.model.SimulateDataService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Locale;



/**
 * @auther getao
 * @date 2024/10/30 18:55
 * @description 模型共有服务实现类
 */
@Service
public class ModelCommonServiceImpl {

    @Autowired
    private SimulateTaskDao taskDao;

    @Autowired
    private AssessmentStatisticDao statisticDao;

    @Autowired
    private ModelService modelService;


    /**
     * @Description 更新仿真任务数据统计信息
     * @auther getao
     * @Date 2024/10/30 18:59
     * @Param [taskId, newDataCount]
     * @Return void
     */
    public void updateSimulsteTaskInfo(int taskId, int newDataCount) {
        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        QueryWrapper<AssessmentStatisticInfo> statisticInfoWra = new QueryWrapper<>();
        statisticInfoWra.eq("task_id", taskId);
        AssessmentStatisticInfo statisticInfo = this.statisticDao.selectOne(statisticInfoWra);
        long createTime = taskInfo.getCreateTime().getTime();
        long currentTime = cn.iecas.simulate.assessment.util.DateUtils.getVariableTime(new Date(), 8).getTime();
        long consumTime =  currentTime - createTime;
        String consumTimeStr = cn.iecas.simulate.assessment.util.DateUtils.millisToTime(consumTime);
        statisticInfo.setTimeConsuming(consumTimeStr);
        int dataCount = statisticInfo.getSimulateDataCount() + newDataCount;
        statisticInfo.setSimulateDataCount(dataCount);
        double avgImportNum = new BigDecimal(dataCount / (consumTime / 1000.0))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        statisticInfo.setAvgImportNum(avgImportNum);
        statisticInfo.setCallCount(statisticInfo.getCallCount()+1);
        double importFrequency = new BigDecimal(statisticInfo.getCallCount() / (consumTime / (1000.0 * 60)))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        statisticInfo.setImportFrequency(importFrequency);
        this.statisticDao.updateById(statisticInfo);
    }


    /**
     * @Description 获取模型仿真数据处理Bean
     * @auther getao
     * @Date 2024/11/1 10:57
     * @Param [modelId]
     * @Return cn.iecas.simulate.assessment.service.model.SimulateDataService
     */
    public SimulateDataService getDataServiceFromModel(Integer modelId) {
        ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        String modelType = modelInfo.getModelType();
        SimulateDataService modelDataTypeService = null;
        if (modelType.equalsIgnoreCase("detection")) {
            modelDataTypeService = ModelDataType.valueOf(modelInfo.getModelType().toUpperCase(Locale.ROOT)).getModelDataTypeService();
        }
        if (modelType.equalsIgnoreCase("dataanalyse")) {
            modelDataTypeService = ModelDataType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT)).getModelDataTypeService();
        }
        return modelDataTypeService;
    }


    /**
     * @Description 获取模型评估处理Bean
     * @auther getao
     * @Date 2024/11/4 15:29
     * @Param [modelId]
     * @Return cn.iecas.simulate.assessment.service.model.SimulateDataService
     */
    public AssessmentService getAnalysisServiceFromModel(Integer modelId) {
        ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        String modelType = modelInfo.getModelType();
        AssessmentService analysisService = null;
        if (modelType.equalsIgnoreCase("detection")) {
            analysisService = AssessmentType.valueOf(modelType.toUpperCase(Locale.ROOT)).getAssessmentTypeService();
        }
        if (modelType.equalsIgnoreCase("dataanalyse")) {
            analysisService = AssessmentType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT)).getAssessmentTypeService();
        }
        if (analysisService == null) {
            throw new RuntimeException("不存在名称为 " + modelInfo.getSign().toUpperCase() + "-SERVICE 的JavaBean...");
        }
        return analysisService;
    }


    /**
     * @Description 根据模型id获取其对应类型的模型服务Bean
     * @Author getao
     * @Date 9:56 2025/3/17
     * @Param [modelId]
     * @return cn.iecas.simulate.assessment.service.model.ModelTypeService
     */
    public ModelTypeService getModelTypeService(int modelId) {
        ModelInfo modelInfo = modelService.getModelInfoById(modelId);
        Assert.notNull(modelInfo, "不存在id为"+modelId+"的模型信息");
        String modelType = modelInfo.getModelType();
        ModelTypeService modelTypeService = null;
        if (modelType.equalsIgnoreCase("detection")) {
            modelTypeService = ModelType.valueOf(modelType.toUpperCase(Locale.ROOT)).getModelTypeService();
        }
        if (modelType.equalsIgnoreCase("dataanalyse")) {
            modelTypeService = ModelType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT)).getModelTypeService();
        }
        if (modelTypeService == null) {
            throw new RuntimeException("不存在名称为 " + modelInfo.getSign().toUpperCase() + "-SERVICE 的JavaBean...");
        }
        return modelTypeService;
    }


    /**
     *  获取一级指标评估结果
     */
    public void getFirstIndexAssessmentResult(IndexResultInfo resultInfo) {
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
     *  获取综合评分
     */
    public void getOverallScore(IndexResultInfo resultInfo) {
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
     * 获取某一级别指标平均得分
     * @param index
     * @return
     */
    public double getIndexAvg(JSONArray index) {
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
