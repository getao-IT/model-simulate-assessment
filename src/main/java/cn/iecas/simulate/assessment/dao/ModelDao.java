package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;



/**
 * @auther cyl
 * @date 2024/8/19
 * @description 模型信息持久类
 */
@Repository
public interface ModelDao extends BaseMapper<TbModelInfo> {

    @Update("UPDATE tb_model_info set status=#{status} where id=#{id}")
    int updateStatusById( @RequestParam("id") Long id, @RequestParam(value = "status") Boolean status);

    /**
     * 使用ServiceType获取与之对应的所有的id
     * @return
     */
    @Select("SELECT service_type, string_agg(id::text, ',') AS ids FROM tb_model_info\n" +
            "WHERE delete = 'f' GROUP BY service_type;")
    @ResultType(Map.class)
    List<Map<String, String>> getIdsGroupByServiceType();

    @Select("SELECT field, STRING_AGG(id::TEXT, ',') AS ids FROM tb_model_info WHERE delete = 'f' GROUP BY field;")
    @ResultType(Map.class)
    List<Map<String, String>> getIdsGroupByField();

    @Select("SELECT DISTINCT unit FROM tb_model_info WHERE system_id IN(SELECT \"id\" FROM tb_system_info WHERE status IS TRUE);")
    List<String> findModelUnits();
}
