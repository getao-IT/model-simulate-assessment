package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.domain.IndexSystemInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.util.List;



/**
 * @auther cyl
 * @date 2024/8/23
 * @description 指标体系信息持久类
 */
@Repository
public interface IndexSystemDao extends BaseMapper<IndexSystemInfo> {

    @Select("SELECT * FROM tb_index_system_info where model_id = #{modelId} and delete = false")
    List<IndexSystemInfo> selectByModelId(Integer modelId);

    @Select("SELECT MAX(batch_no) FROM tb_index_system_info WHERE model_id=#{modelId}")
    int selectMaxBatchNoByModelId(int modelId);
}
