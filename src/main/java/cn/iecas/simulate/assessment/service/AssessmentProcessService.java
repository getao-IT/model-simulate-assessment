package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.domain.AssessmentProcessInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Collection;
import java.util.List;



/**
 *  @author: getao
 *  @Date: 2024/10/12 16:29
 *  @Description: 模型评估过程服务类
 */
public interface AssessmentProcessService {

    boolean batchInsert(Collection<AssessmentProcessInfo> entityList);

    List<AssessmentProcessInfo> listByQueryWrapper(QueryWrapper<AssessmentProcessInfo> wrapper);
}
