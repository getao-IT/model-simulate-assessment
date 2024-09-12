package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.domain.IndexInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.util.List;



/**
 * @auther getao
 * @Date 2024/8/29 16:46
 * @Description 模型指标信息持久类
 */
@Repository
public interface IndexInfoDao extends BaseMapper<IndexInfo> {

    @Select("SELECT* FROM tb_index_info WHERE sign = #{sign} AND delete = false")
    List<IndexInfo> selectBySign(String sign);

    @Select("select * from tb_index_info where sign=#{sign}")
    List<IndexInfo> findBySign(String sign);

    @Select("select * from tb_index_info where id = #{id}")
    IndexInfo findById(Integer id);
}
