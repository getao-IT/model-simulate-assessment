package cn.iecas.simulate.assessment.dao;

import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;



/**
 * @auther getao
 * @date 2024/8/23
 * @description TODO getao
 */
@Repository
public interface SimulateDataDao extends BaseMapper<SimulateDataInfo> {


    // 根据task_id查询所有的import_time
    @Select("SELECT import_time FROM tb_simulate_data_info WHERE task_id = #{taskId}")
    List<Date> selectImportTimesByTaskId(Integer taskId);
}
