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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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


    /**
     * @auther cyl
     * @Date 2024/10/9 11:06
     * @Description 获取模型仿真数据引接趋势变化信息
     */
    @Override
    public Map<String, Long> getImportTrendByTaskId(Integer taskId) {
        List<Date> importTimes = dataDao.selectImportTimesByTaskId(taskId);
        if (importTimes.isEmpty()) {
            return new TreeMap<>();
        }
        long minTime = importTimes.stream().mapToLong(Date::getTime).min().orElse(0L);
        long maxTime = importTimes.stream().mapToLong(Date::getTime).max().orElse(0L);
        Map<String, Long> resultMap = new TreeMap<>();
        long cumulativeCount = 0;
        for (long startTime = minTime; startTime <= maxTime; startTime += 100) {
            long endTime = startTime + 100;
            String key = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(startTime));
            long finalStartTime = startTime;
            long countInThisInterval = importTimes.stream()
                    .filter(time -> time.getTime() >= finalStartTime && time.getTime() < endTime)
                    .count();
            if (countInThisInterval > 0) {
                cumulativeCount += countInThisInterval;
                resultMap.put(key, cumulativeCount);
            }
        }
        return resultMap;
    }


    @Override
    public List<SimulateDataInfo> getSimulateDataByModel(int taskId, int modelId) {
        QueryWrapper<SimulateDataInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).eq("model_id", modelId);
        List<SimulateDataInfo> simulateDataInfos = this.dataDao.selectList(wrapper);
        return simulateDataInfos;
    }
}
