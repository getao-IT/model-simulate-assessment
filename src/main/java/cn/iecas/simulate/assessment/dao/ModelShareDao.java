package cn.iecas.simulate.assessment.dao;

import cn.iecas.simulate.assessment.entity.domain.ModelShareInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface ModelShareDao extends BaseMapper<ModelShareInfo> {

    // 获取去重后的评估记录总数
    @Select("SELECT COUNT(DISTINCT id) FROM tb_model_share_info WHERE delete = false")
    Integer getAssessmentTotal();

    // 获取去重后的模型总数
    @Select("SELECT COUNT(DISTINCT model_id) FROM tb_model_share_info WHERE delete = false")
    Integer getModelTotal();
}
