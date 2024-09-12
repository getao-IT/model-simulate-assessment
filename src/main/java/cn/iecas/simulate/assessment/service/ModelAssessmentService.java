package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelAssessmentDto;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Date;
import java.util.List;
import java.util.Map;



/**
 * @auther guoxun
 * @Date 2024/8/27 17:26
 * @Description 模型仿真评估记录接口
 */
public interface ModelAssessmentService extends IService<ModelAssessmentInfo> {

    PageResult<ModelAssessmentInfo> getAssessmentHistory(ModelAssessmentDto modelAssessmentDto);

    Map<String, List<Map<String, Object>>> getModelApplyStatistic();

    List<Map<String, Object>> getModelApplyTrend(String mode);

    Integer getTotalModels();

    Integer getFinishModels();

    Integer getUnfinishedModels();

    Integer getTotalEvaluations();

    ModelAssessmentInfo getModelAssessmentInfoBytask(int taskId, int modelId);
}
