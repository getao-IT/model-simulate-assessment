package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;



/**
 * @auther getao
 * @Date 2024/8/27 15:56
 * @Description 模型评估记录持久类
 */
@Repository
public interface ModelAssessmentDao extends BaseMapper<ModelAssessmentInfo> {

    @Select("SELECT COUNT(DISTINCT model_id) FROM tb_model_assessment_info")
    Integer countTotalModels();

    @Select("SELECT COUNT(model_id) FROM tb_model_assessment_info WHERE status = 'FINISH'")
    Integer countFinishedModels();

    @Select("SELECT COUNT(model_id) FROM tb_model_assessment_info WHERE status = 'FAIL'")
    Integer countUnFinishedModels();

    @Select("SELECT COUNT(*) FROM tb_model_assessment_info")
    Integer countTotalEvaluations();
}
