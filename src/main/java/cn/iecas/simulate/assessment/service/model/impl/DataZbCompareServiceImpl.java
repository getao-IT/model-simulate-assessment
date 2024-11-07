package cn.iecas.simulate.assessment.service.model.impl;

import cn.iecas.simulate.assessment.dao.model.ZbCompareDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import cn.iecas.simulate.assessment.entity.model.domain.ZbCompareInfo;
import cn.iecas.simulate.assessment.service.model.SimulateDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import java.util.stream.Collectors;


/**
 * @auther getao
 * @Date 2024/8/23 15:06
 * @Description 中美对比详情服务接口实现类
 */
@Service(value = "ZMDB-COMPARE-DATASERVICE")
public class DataZbCompareServiceImpl extends ServiceImpl<ZbCompareDao, ZbCompareInfo> implements SimulateDataService<ZbCompareInfo> {

    @Autowired
    private ZbCompareDao compareDao;


    @Override
    public boolean insertBatch(List<ZbCompareInfo> dataInfos) {
        boolean insert = this.saveBatch(dataInfos);
        return insert;
    }


    @Override
    public PageResult<ZbCompareInfo> listSimulateData(SimulateDataInfoDto dataInfo) {
        IPage<ZbCompareInfo> page = new Page<>(dataInfo.getPageNo(), dataInfo.getPageSize());
        QueryWrapper<ZbCompareInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", dataInfo.getTaskId())
                .and(e -> e.eq("model_id", dataInfo.getModelId()))
                .like(dataInfo.getFuzzy() != null, "CONCAT(country_name, name, children)", dataInfo.getFuzzy())
                .orderByDesc("import_time");
        IPage<ZbCompareInfo> dataInfos = compareDao.selectPage(page, wrapper);
        return new PageResult<ZbCompareInfo>(dataInfos.getCurrent(), dataInfos.getTotal(), dataInfos.getRecords());
    }


    /**
     *  @author: getao
     *  @Date: 2024/10/22 16:19
     *  @Description: 更新数据所在任务为当前运行的任务
     */
    @Override
    public void updateDataInTheTask(Integer taskId, Integer modelId) {
        UpdateWrapper<ZbCompareInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("task_id", taskId).set("model_id", modelId);
        this.update(updateWrapper);
    }


    /**
     * @auther cyl
     * @Date 2024/10/9 11:06
     * @Description 获取模型仿真数据引接趋势变化信息
     */
    @Override
    public Map<String, Long> getImportTrendByTaskId(Integer taskId) {
        QueryWrapper<ZbCompareInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("import_time");
        List<Date> importTimes = this.compareDao.selectList(queryWrapper).stream().map(ZbCompareInfo::getImportTime).collect(Collectors.toList());

        if (importTimes.isEmpty()) {
            return new TreeMap<>();
        }
        long minTime = importTimes.stream().mapToLong(Date::getTime).min().orElse(0L);
        long maxTime = importTimes.stream().mapToLong(Date::getTime).max().orElse(0L);
        long statisticOffset = this.getStatisticOffset(maxTime, minTime);
        Map<String, Long> resultMap = new TreeMap<>();
        long cumulativeCount = 0;
        for (long startTime = minTime; startTime <= maxTime; startTime += statisticOffset) {
            long endTime = startTime + statisticOffset;
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
    
    
    /**
     *  @author: getao
     *  @Date: 2024/10/23 15:11
     *  @Description: 获取仿真数据统计维度基准数
     */ 
    private long getStatisticOffset(long maxTime, long minTime) {
        long consumeTime = maxTime - minTime;
        if (consumeTime > (long)3600000 * 24 * 30 * 12) {
            return (long)3600000 * 24 * 30;
        } else if (consumeTime > (long)3600000 * 24 * 30) {
            return 3600000 * 24;
        } else if (consumeTime > (long)3600000 * 24) {
            return 1000 * 60 * 5;
        } else if (consumeTime > 3600000) {
            return 1000 * 10;
        }
        return 10;
    }


    @Override
    public List<ZbCompareInfo> getSimulateDataByModel(int taskId, int modelId) {
        QueryWrapper<ZbCompareInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).eq("model_id", modelId);
        List<ZbCompareInfo> simulateDataInfos = this.compareDao.selectList(wrapper);
        return simulateDataInfos;
    }
}
