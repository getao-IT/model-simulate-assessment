package cn.iecas.simulate.assessment.dao;


import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;



/**
 * @auther getao
 * @Date 2024/8/27 15:56
 * @Description 仿真任务统计持久类
 */
@Repository
public interface AssessmentStatisticDao extends BaseMapper<AssessmentStatisticInfo> {
}
