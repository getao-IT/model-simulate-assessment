package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.SimulateAssessmentStatisticDao;
import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentStatisticInfo;
import cn.iecas.simulate.assessment.service.SimulateAssessmentStatisticService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * @auther cyl
 * @date 2024/8/28
 * @description 模型仿真评估统计接口实现类
 */
@Service
public class SimulateAssessmentStatisticServiceImpl extends ServiceImpl<SimulateAssessmentStatisticDao, ModelAssessmentStatisticInfo> implements SimulateAssessmentStatisticService {

    @Autowired
    private SimulateAssessmentStatisticDao simulateAssessmentStatisticDao;


    @Override
    public ModelAssessmentStatisticInfo getSimulateDataByTaskId(Integer taskId) {
        return  simulateAssessmentStatisticDao.findByTaskId(taskId);
    }
}
