package cn.iecas.simulate.assessment.service.impl;


import cn.iecas.simulate.assessment.dao.AssessmentProcessDao;
import cn.iecas.simulate.assessment.entity.domain.AssessmentProcessInfo;
import cn.iecas.simulate.assessment.service.AssessmentProcessService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;



/**
 *  @author: getao
 *  @Date: 2024/10/12 16:29
 *  @Description: 模型评估过程服务实现类
 */
@Service
public class AssessmentProcessServiceImpl extends ServiceImpl<AssessmentProcessDao, AssessmentProcessInfo> implements AssessmentProcessService {


    @Override
    @Transactional
    public boolean batchInsert(Collection<AssessmentProcessInfo> entityList) {
        return this.saveBatch(entityList);
    }
}
