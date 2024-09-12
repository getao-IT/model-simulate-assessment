package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.SimulateDataDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import cn.iecas.simulate.assessment.service.SimulateDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;



/**
 * @auther getao
 * @Date 2024/8/23 15:06
 * @Description 仿真数据服务接口实现类
 */
@Service
public class SimulateDataServiceImpl extends ServiceImpl<SimulateDataDao, SimulateDataInfo> implements SimulateDataService {

    @Autowired
    private SimulateDataDao dataDao;


    @Override
    public boolean insertBatch(List<SimulateDataInfo> dataInfos) {
        boolean insert = this.saveBatch(dataInfos);
        return insert;
    }


    @Override
    public PageResult<SimulateDataInfo> listSimulateData(SimulateDataInfoDto dataInfo) {
        IPage<SimulateDataInfo> page = new Page<>(dataInfo.getPageNo(), dataInfo.getPageSize());
        QueryWrapper<SimulateDataInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", dataInfo.getTaskId())
                .and(e -> e.eq("model_id", dataInfo.getModelId()))
                .like(dataInfo.getFuzzy() != null, "CONCAT(title_zh, direction, territory, keyword" +
                        ", co_proposer, committee)", dataInfo.getFuzzy())
                .orderByDesc("import_time");
        IPage<SimulateDataInfo> dataInfos = dataDao.selectPage(page, wrapper);
        return new PageResult<SimulateDataInfo>(dataInfos.getCurrent(), dataInfos.getTotal(), dataInfos.getRecords());
    }


    @Override
    public List<Map<String ,Object>> getImportTrendByTaskId(Integer taskId) {
        return dataDao.getImportTrendByTaskId(taskId);
    }


    @Override
    public List<SimulateDataInfo> getSimulateDataByModel(int taskId, int modelId) {
        QueryWrapper<SimulateDataInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).eq("model_id", modelId);
        List<SimulateDataInfo> simulateDataInfos = this.dataDao.selectList(wrapper);
        return simulateDataInfos;
    }
}
