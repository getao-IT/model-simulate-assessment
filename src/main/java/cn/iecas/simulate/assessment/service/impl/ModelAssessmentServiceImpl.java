package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.ModelAssessmentDao;
import cn.iecas.simulate.assessment.dao.ModelDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelAssessmentDto;
import cn.iecas.simulate.assessment.service.ModelAssessmentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @auther guoxun
 * @Date 2024/8/27 17:23
 * @Description 模型仿真评估记录接口实现类
 */
@Service
public class ModelAssessmentServiceImpl extends ServiceImpl<ModelAssessmentDao, ModelAssessmentInfo> implements ModelAssessmentService {

    @Autowired
    private ModelDao modelDao;

    @Autowired
    private ModelAssessmentDao modelAssessmentDao;

    @Autowired
    private SimulateTaskDao simulateTaskDao;


    @Override
    public PageResult<ModelAssessmentInfo> getAssessmentHistory(ModelAssessmentDto modelAssessmentDto) {
        QueryWrapper<ModelAssessmentInfo> wrapper = new QueryWrapper<>();
        if (modelAssessmentDto.getSceneName() != null)
            wrapper.eq("scene_name", modelAssessmentDto.getSceneName());
        if (modelAssessmentDto.getUnit() != null)
            wrapper.eq("unit", modelAssessmentDto.getUnit());
        if (modelAssessmentDto.getModelName() != null)
            wrapper.eq("model_name", modelAssessmentDto.getModelName());
        if (modelAssessmentDto.getCreater() != null)
            wrapper.eq("creater", modelAssessmentDto.getCreater());
        if (modelAssessmentDto.getStatus() != null)
            wrapper.eq("status", modelAssessmentDto.getStatus());
        // 若传递下述两个时间字段，则为精确匹配时间
        if (modelAssessmentDto.getFinishTime() != null)
            wrapper.eq("finish_time", modelAssessmentDto.getFinishTime());
        if (modelAssessmentDto.getCreateTime() != null)
            wrapper.eq("create_time", modelAssessmentDto.getCreateTime());
        // 下述为按范围进行查询
        if (modelAssessmentDto.getLeCreateTime() != null)
            wrapper.le("create_time", modelAssessmentDto.getLeCreateTime());
        if (modelAssessmentDto.getGeCreateTime() != null)
            wrapper.ge("create_time", modelAssessmentDto.getGeCreateTime());
        if (modelAssessmentDto.getLeFinishTime() != null)
            wrapper.le("finish_time", modelAssessmentDto.getLeFinishTime());
        if (modelAssessmentDto.getGeFinishTime() != null)
            wrapper.ge("finish_time", modelAssessmentDto.getGeFinishTime());
        wrapper.orderByDesc(modelAssessmentDto.getOrderCol() == null, "id");
        wrapper.orderByDesc(modelAssessmentDto.getOrderCol() != null
                && modelAssessmentDto.getOrderWay().equalsIgnoreCase("desc")
                , modelAssessmentDto.getOrderCol());
        wrapper.orderByAsc(modelAssessmentDto.getOrderCol() != null
                && modelAssessmentDto.getOrderWay().equalsIgnoreCase("asc")
                , modelAssessmentDto.getOrderCol());
        IPage<ModelAssessmentInfo> result = baseMapper.selectPage(
                new Page<>(modelAssessmentDto.getPageNo(), modelAssessmentDto.getPageSize()), wrapper);
        return new PageResult<>(result.getCurrent(), result.getTotal(), result.getRecords());
    }


    @Override
    public Integer getTotalModels() {
        return modelAssessmentDao.countTotalModels();
    }


    @Override
    public Integer getFinishModels() {
        return modelAssessmentDao.countFinishedModels();
    }


    @Override
    public Integer getUnfinishedModels() {
        return modelAssessmentDao.countUnFinishedModels();
    }


    @Override
    public Integer getTotalEvaluations() {
        return modelAssessmentDao.countTotalEvaluations();
    }


    @Override
    public Integer deleteHistoryByTaskId(Integer taskId) {
        QueryWrapper<ModelAssessmentInfo> delete = new QueryWrapper<>();
        delete.eq("task_id", taskId);
        this.modelAssessmentDao.delete(delete);
        return taskId;
    }


    /**
    * @Description 根据任务id和模型id获取模型仿真记录信息
    * @auther getao
    * @Date 2024/9/4 15:26
    * @Param [taskId, modelId]
    * @Return
    */
    @Override
    public ModelAssessmentInfo getModelAssessmentInfoBytask(int taskId, int modelId) {
        QueryWrapper<ModelAssessmentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).eq("model_id", modelId);
        ModelAssessmentInfo assessmentInfo = this.baseMapper.selectList(wrapper).stream().findFirst().get();
        return assessmentInfo;
    }


    /**
     * 此方法为getModelApplyStatistic方法提供服务，返回所需的map对象
     */
    public Map<String, Object> getSubResultMap(String name, Double percent, Integer count){
        Map<String, Object> subResultMap = new HashMap<>();
        subResultMap.put("name", name);
        subResultMap.put("percent", percent);
        subResultMap.put("count", count);
        return subResultMap;
    }


    @Override
    public Map<String, List<Map<String, Object>>> getModelApplyStatistic() {
        List<Map<String, String>> idsGroupByServiceTypeList = modelDao.getIdsGroupByServiceType();
        Map<String, List<Integer>> serviceTypeIdsMap = idsGroupByServiceTypeList.stream().collect(Collectors.toMap(
                map -> map.get("serviceType"),
                map -> {
                    List<Integer> list = new ArrayList<>();
                    String[] ids = map.get("ids").split(",");
                    for (String e : ids){
                        list.add(Integer.parseInt(e));
                    }
                    return list;
                }
        ));
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("result", new ArrayList<>());
        // 查询总数
        Integer totalCount = baseMapper.selectCount(new QueryWrapper<>());
        // 查询对应类型的模型比率和数量
        for (String key : serviceTypeIdsMap.keySet()){
            Integer count = baseMapper.selectCount(new QueryWrapper<ModelAssessmentInfo>()
                    .in("model_id", serviceTypeIdsMap.get(key)));
            result.get("result").add(getSubResultMap(key
                    , totalCount == 0 ? 0.0 : (double)count / totalCount, count));
        }
        return result;
    }


    /**
     * getModelApplyTrend方法的工具类，获取以给定时间为基准的，前或后的某个月的第一天的Calendar对象
     */
    private Calendar getFirstDayOfMonthCalendar(Calendar calendar, Integer offset){
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(calendar.getTime());
        newCalendar.add(Calendar.MONTH, offset);
        newCalendar.set(Calendar.DAY_OF_MONTH, 1);
        newCalendar.set(Calendar.HOUR_OF_DAY, 0);
        newCalendar.set(Calendar.MINUTE, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        return newCalendar;
    }


    /**
     * 获取指定年份的第一天
     */
    private Calendar getFirstDayOfYearCalendar(Calendar calendar, int offset){
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(calendar.getTime());
        newCalendar.add(Calendar.YEAR, offset);
        newCalendar.set(Calendar.MONTH, 1);
        newCalendar.set(Calendar.DAY_OF_MONTH, 0);
        newCalendar.set(Calendar.HOUR_OF_DAY, 0);
        newCalendar.set(Calendar.MINUTE, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        return newCalendar;
    }


    /**
     * 周日期计算
     */
    private Calendar getFirstDayOfWeekCalendar(Calendar calendar, int offset){
        Calendar newCalendar = Calendar.getInstance();
        newCalendar.setTime(calendar.getTime());
        newCalendar.add(Calendar.WEEK_OF_YEAR, offset);
        newCalendar.set(Calendar.DAY_OF_WEEK, 1);
        newCalendar.set(Calendar.HOUR_OF_DAY, 0);
        newCalendar.set(Calendar.MINUTE, 0);
        newCalendar.set(Calendar.MILLISECOND, 0);
        return newCalendar;
    }


    private Map<String, Object> getModelApplyTrendData(String field, List<Integer> ids, String mode){
        if (!mode.equalsIgnoreCase("year") && !mode.equalsIgnoreCase("month")
                && !mode.equalsIgnoreCase("week")){
            throw new RuntimeException("mode参数仅能为year, month, week");
        }
        ModelAssessmentInfo leTimeObj = baseMapper.selectOne(new QueryWrapper<ModelAssessmentInfo>()
                .select("MAX(create_time) AS create_time").in("model_id", ids));
        ModelAssessmentInfo geTimeObj = baseMapper.selectOne(new QueryWrapper<ModelAssessmentInfo>()
                .select("MIN(create_time) AS create_time").in("model_id", ids));
        if (geTimeObj == null || leTimeObj == null)
            return null;
        if (geTimeObj.getCreateTime() == null || leTimeObj.getCreateTime() == null)
            return null;
        Date geTime = geTimeObj.getCreateTime();
        Date leTime = leTimeObj.getCreateTime();
        LocalDate geLocalDate = geTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate leLocalDate = leTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Period period = Period.between(geLocalDate, leLocalDate);
        int monthsBetween = period.getYears() * 12 + period.getMonths();
        int yearsBetween = period.getYears() + 1;
        int weeksBetween = (int)Math.ceil(ChronoUnit.DAYS.between(geLocalDate, leLocalDate) / 7.0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(geTime);
        List<String> timeList = new ArrayList<>();
        List<Integer> countList = new ArrayList<>();
        if (mode.equalsIgnoreCase("month")) {
            SimpleDateFormat simpleDateMonthFormat = new SimpleDateFormat("yyyy-MM");
            for (int i = 0; i <= monthsBetween; ) {
                Calendar geCalendar = getFirstDayOfMonthCalendar(calendar, i);
                Calendar leCalendar = getFirstDayOfMonthCalendar(calendar, ++i);
                Integer count = baseMapper.selectCount(new QueryWrapper<ModelAssessmentInfo>().in("model_id", ids)
                        .ge("create_time", geCalendar.getTime())
                        .le("create_time", leCalendar.getTime()));
                timeList.add(simpleDateMonthFormat.format(geCalendar.getTime()));
                countList.add(count);
            }
        }
        else if (mode.equalsIgnoreCase("week")){
            SimpleDateFormat simpleDateWeekFormat = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i <= weeksBetween;){
                Calendar geCalendar = getFirstDayOfWeekCalendar(calendar, i);
                Calendar leCalendar = getFirstDayOfWeekCalendar(calendar, ++i);
                Integer count = baseMapper.selectCount(new QueryWrapper<ModelAssessmentInfo>().in("model_id", ids)
                        .ge("create_time", geCalendar.getTime())
                        .le("create_time", leCalendar.getTime()));
                timeList.add(simpleDateWeekFormat.format(geCalendar.getTime()));
                countList.add(count);
            }
        }
        else if (mode.equalsIgnoreCase("year")){
            SimpleDateFormat simpleDateYearFormat = new SimpleDateFormat("yyyy");
            for (int i = 0; i < yearsBetween;){
                Calendar geCalendar = getFirstDayOfYearCalendar(calendar, i);
                Calendar leCalendar = getFirstDayOfYearCalendar(calendar, ++i);
                Integer count = baseMapper.selectCount(new QueryWrapper<ModelAssessmentInfo>().in("model_id", ids)
                        .ge("create_time", geCalendar.getTime())
                        .le("create_time", leCalendar.getTime()));
                timeList.add(simpleDateYearFormat.format(geCalendar.getTime()));
                countList.add(count);
            }
        }
        Map<String, Object> res = new HashMap<>();
        res.put("time", timeList);
        res.put("count", countList);
        res.put("field", field);
        return res;
    }


    @Override
    public List<Map<String, Object>> getModelApplyTrend(String mode) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, String>> idsGroupByField = modelDao.getIdsGroupByServiceType();
        Map<String, List<Integer>> idsGroupByFieldMap = idsGroupByField.stream().collect(Collectors.toMap(
                map -> map.get("serviceType"),
                map -> {
                    List<Integer> list = new ArrayList<>();
                    String[] ids = map.get("ids").split(",");
                    for (String e : ids){
                        list.add(Integer.parseInt(e));
                    }
                    return list;
                }
        ));
        for (String key : idsGroupByFieldMap.keySet()){
            Map<String, Object> subResult = getModelApplyTrendData(key, idsGroupByFieldMap.get(key), mode);
            if (subResult == null)
                continue;
            result.add(subResult);
        }
        return result;
    }
}
