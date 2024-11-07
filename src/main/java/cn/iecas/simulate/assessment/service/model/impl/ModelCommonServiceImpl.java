package cn.iecas.simulate.assessment.service.model.impl;

import cn.iecas.simulate.assessment.dao.AssessmentStatisticDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.entity.model.emun.AssessmentType;
import cn.iecas.simulate.assessment.entity.model.emun.ModelDataType;
import cn.iecas.simulate.assessment.service.ModelService;
import cn.iecas.simulate.assessment.service.model.AssessmentService;
import cn.iecas.simulate.assessment.service.model.SimulateDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        TbModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        ModelDataType modelDataType = ModelDataType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT));
        SimulateDataService modelDataTypeService = modelDataType.getModelDataTypeService();
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
        TbModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        AssessmentType assessmentType = AssessmentType.valueOf(modelInfo.getSign().toUpperCase(Locale.ROOT));
        AssessmentService analysisService = assessmentType.getAssessmentTypeService();
        return analysisService;
    }
}
