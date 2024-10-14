package cn.iecas.simulate.assessment.dao;

import cn.iecas.simulate.assessment.entity.domain.AssessmentProcessInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;



/**
 *  @author: getao
 *  @Date: 2024/10/12 16:22
 *  @Description: 评估过程记录持久类
 */
@Repository
public interface AssessmentProcessDao extends BaseMapper<AssessmentProcessInfo> {
}
