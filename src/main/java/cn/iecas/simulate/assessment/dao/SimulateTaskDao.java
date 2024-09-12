package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;



/**
 * @auther getao
 * @Date 2024/8/23 9:09
 * @Description 仿真任务持久类
 */
@Repository
public interface SimulateTaskDao extends BaseMapper<SimulateTaskInfo> {
}
