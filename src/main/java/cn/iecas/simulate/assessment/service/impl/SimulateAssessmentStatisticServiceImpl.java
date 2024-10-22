package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.SimulateAssessmentStatisticDao;
import cn.iecas.simulate.assessment.entity.domain.AssessmentStatisticInfo;
import cn.iecas.simulate.assessment.service.SimulateAssessmentStatisticService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;



/**
 * @auther cyl
 * @date 2024/8/28
 * @description 模型仿真评估统计接口实现类
 */
@Service
public class SimulateAssessmentStatisticServiceImpl extends ServiceImpl<SimulateAssessmentStatisticDao, AssessmentStatisticInfo> implements SimulateAssessmentStatisticService {

    @Autowired
    private SimulateAssessmentStatisticDao simulateAssessmentStatisticDao;


    @Override
    public AssessmentStatisticInfo getSimulateDataByTaskId(Integer taskId) {
        return  simulateAssessmentStatisticDao.findByTaskId(taskId);
    }


    @Override
    public AssessmentStatisticInfo updateAdjustNum(Integer taskId) {
        AssessmentStatisticInfo statisticInfo = null;
        QueryWrapper<AssessmentStatisticInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_id", taskId);
        List<AssessmentStatisticInfo> statisticInfos = this.simulateAssessmentStatisticDao.selectList(queryWrapper);
        if (statisticInfos.size() == 0) {
            return null;
        } else {
            statisticInfo = statisticInfos.stream().findFirst().get();
        }
        statisticInfo.setFrequencyAdjustNum(statisticInfo.getFrequencyAdjustNum()+1);
        this.updateById(statisticInfo);
        return statisticInfo;
    }
}
