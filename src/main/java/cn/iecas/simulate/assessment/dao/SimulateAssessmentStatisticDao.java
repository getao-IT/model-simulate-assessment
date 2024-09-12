package cn.iecas.simulate.assessment.dao;

import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentStatisticInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;



/**
 * @auther cyl
 * @date 2024/8/28
 * @description 仿真评估统计持久类
 */
@Repository
public interface SimulateAssessmentStatisticDao extends BaseMapper<ModelAssessmentStatisticInfo> {

    @Select("SELECT * FROM tb_model_assessment_statistic_info WHERE task_id=#{taskId}")
    ModelAssessmentStatisticInfo findByTaskId(Integer taskId);
}
