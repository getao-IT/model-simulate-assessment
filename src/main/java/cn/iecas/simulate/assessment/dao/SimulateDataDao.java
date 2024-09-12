package cn.iecas.simulate.assessment.dao;

import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;



/**
 * @auther getao
 * @date 2024/8/23
 * @description TODO getao
 */
@Repository
public interface SimulateDataDao extends BaseMapper<SimulateDataInfo> {

    @Select("select import_time AS date,SUM(counts) OVER (ORDER BY import_time ASC) AS total_count "+
    " FROM ( "+
    " select import_time,count(*) AS counts " +
    " FROM tb_simulate_data_info "+
    " WHERE task_id= #{taskId} "+
    " group by import_time " +
    ") AS daily_counts " +
    "ORDER BY import_time ASC")
    List<Map<String,Object>> getImportTrendByTaskId(Integer taskId);
}
