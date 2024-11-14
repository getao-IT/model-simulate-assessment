package cn.iecas.simulate.assessment.service.model.impl;

import cn.iecas.simulate.assessment.dao.model.IndexIndicatorTaskDao;
import cn.iecas.simulate.assessment.dao.model.ZbCompareDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import cn.iecas.simulate.assessment.entity.model.domain.IndexIndicatorTaskInfo;
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
import java.util.*;
import java.util.stream.Collectors;


/**
 * @auther getao
 * @Date 2024/8/23 15:06
 * @Description 中美对比仿真数据服务接口实现类
 */
@Service(value = "ZMDB-TASK-DATASERVICE")
public class DataIndexIndicatorTaskServiceImpl extends ServiceImpl<IndexIndicatorTaskDao, IndexIndicatorTaskInfo> implements SimulateDataService<IndexIndicatorTaskInfo> {

    @Autowired
    private IndexIndicatorTaskDao taskDao;

    @Autowired
    private ZbCompareDao compareDao;


    @Override
    public boolean insertBatch(List<IndexIndicatorTaskInfo> dataInfos) {
        boolean insert = this.saveBatch(dataInfos);
        return insert;
    }


    @Override
    public PageResult<IndexIndicatorTaskInfo> listSimulateData(SimulateDataInfoDto dataInfo) {
        IPage<IndexIndicatorTaskInfo> page = new Page<>(dataInfo.getPageNo(), dataInfo.getPageSize());
        QueryWrapper<IndexIndicatorTaskInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", dataInfo.getTaskId())
                .and(e -> e.eq("model_id", dataInfo.getModelId()))
                .like(dataInfo.getFuzzy() != null, "CONCAT(taskname, model_type)", dataInfo.getFuzzy())
                .orderByDesc("import_time");
        IPage<IndexIndicatorTaskInfo> dataInfos = taskDao.selectPage(page, wrapper);

        List<IndexIndicatorTaskInfo> records = dataInfos.getRecords();
        for (IndexIndicatorTaskInfo record : records) {
            QueryWrapper<ZbCompareInfo> compareWrapper = new QueryWrapper<>();
            compareWrapper.eq("task_id", dataInfo.getTaskId()).eq("model_id", dataInfo.getModelId())
                    .eq("indicator_task_id", record.getId());
            List<ZbCompareInfo> compareInfos = this.compareDao.selectList(compareWrapper);
            record.setCompareInfos(compareInfos);
        }

        String[] columnArrs = {"id","modelType","participants","platform","process","specialIdent","startdate","status"
                ,"taskname","topicType","yearArr","zbxh","countryArr","defaultState","enddate","image","importTime"};
        List<String> cols = Arrays.stream(columnArrs).collect(Collectors.toList());
        return new PageResult<IndexIndicatorTaskInfo>(dataInfos.getCurrent(), dataInfos.getTotal(), records, cols);
    }


    /**
     *  @author: getao
     *  @Date: 2024/10/22 16:19
     *  @Description: 更新数据所在任务为当前运行的任务
     */
    @Override
    public void updateDataInTheTask(Integer taskId, Integer modelId) {
        UpdateWrapper<IndexIndicatorTaskInfo> updateWrapper = new UpdateWrapper<>();
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
        QueryWrapper<IndexIndicatorTaskInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("import_time");
        List<Date> importTimes = this.taskDao.selectList(queryWrapper).stream().map(IndexIndicatorTaskInfo::getImportTime).collect(Collectors.toList());
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
    public List<IndexIndicatorTaskInfo> getSimulateDataByModel(int taskId, int modelId) {
        QueryWrapper<IndexIndicatorTaskInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).eq("model_id", modelId);
        List<IndexIndicatorTaskInfo> simulateDataInfos = this.taskDao.selectList(wrapper);

        for (IndexIndicatorTaskInfo simulateData : simulateDataInfos) {
            QueryWrapper<ZbCompareInfo> compareWrapper = new QueryWrapper<>();
            compareWrapper.eq("task_id", simulateData.getTaskId()).eq("model_id", simulateData.getModelId())
                    .eq("indicator_task_id", simulateData.getId());
            List<ZbCompareInfo> compareInfos = this.compareDao.selectList(compareWrapper);
            simulateData.setCompareInfos(compareInfos);
        }
        return simulateDataInfos;
    }
}
