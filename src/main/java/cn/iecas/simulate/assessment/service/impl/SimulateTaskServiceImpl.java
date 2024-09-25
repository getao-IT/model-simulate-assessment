package cn.iecas.simulate.assessment.service.impl;

import cn.aircas.utils.date.DateUtils;
import cn.iecas.simulate.assessment.dao.AssessmentStatisticDao;
import cn.iecas.simulate.assessment.dao.ModelAssessmentDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.*;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import cn.iecas.simulate.assessment.service.*;
import cn.iecas.simulate.assessment.util.CollectionsUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;



/**
 * @auther getao
 * @Date 2024/8/23 9:12
 * @Description 仿真任务服务接口实现类
 */
@Slf4j
@Service
public class SimulateTaskServiceImpl extends ServiceImpl<SimulateTaskDao, SimulateTaskInfo> implements SimulateTaskService {

    @Autowired
    private SimulateTaskDao taskDao;

    @Autowired
    private AssessmentStatisticDao statisticDao;

    @Autowired
    private ModelAssessmentDao modelAssessmentDao;

    @Autowired
    private RestTemplateApi templateApi;

    @Autowired
    private SimulateDataService dataService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private SimulateDataAnalysisService analysisService;

    @Autowired
    private SceneService sceneService;


   /**
    * @Description 分页获取仿真任务信息
    * @auther getao
    * @Date 2024/8/30 11:09
    * @Param [taskInfoDto]
    * @Return
    */
    @Override
    public PageResult<SimulateTaskInfo> getSimulateTaskInfo(SimulateTaskInfoDto taskInfoDto) {
        IPage<SimulateTaskInfo> page = new Page<>(taskInfoDto.getPageNo(), taskInfoDto.getPageSize());
        QueryWrapper<SimulateTaskInfo> wrapper = new QueryWrapper<>();
        wrapper.eq(taskInfoDto.getTaskName() != null, "task_name", taskInfoDto.getTaskName())
                .eq(taskInfoDto.getTaskType() != null, "task_type", taskInfoDto.getTaskType())
                .eq(taskInfoDto.getUserLevel() != null, "user_level", taskInfoDto.getUserLevel())
                .eq(taskInfoDto.getField() != null, "field", taskInfoDto.getField())
                .eq(taskInfoDto.getSceneName() != null, "scene_name", taskInfoDto.getSceneName())
                .eq(taskInfoDto.getModelName() != null, "model_name", taskInfoDto.getModelName())
                .eq(taskInfoDto.getCreater() != null, "creater", taskInfoDto.getCreater())
                .le(taskInfoDto.getLeTime() != null, "create_time", taskInfoDto.getLeTime())
                .ge(taskInfoDto.getGeTime() != null, "create_time", taskInfoDto.getGeTime())
                .like(taskInfoDto.getFuzzy() != null, "CONCAT(task_name, user_level, scene_name, model_name" +
                        ",creater, describe)", taskInfoDto.getFuzzy())
                .orderByDesc(taskInfoDto.getOrderCol() != null
                        && taskInfoDto.getOrderWay().equalsIgnoreCase("desc"), taskInfoDto.getOrderCol())
                .orderByAsc(taskInfoDto.getOrderCol() != null
                        && taskInfoDto.getOrderWay().equalsIgnoreCase("asc"), taskInfoDto.getOrderCol());
        IPage<SimulateTaskInfo> taskInfos = taskDao.selectPage(page, wrapper);
        return new PageResult<>(taskInfos.getCurrent(), taskInfos.getTotal(), taskInfos.getRecords());
    }


   /**
    * @Description 新增仿真任务信息
    * @auther getao
    * @Date 2024/8/30 11:09
    * @Param [taskInfo]
    * @Return
    */
    @Override
    @Transactional
    public SimulateTaskInfo saveSimulate(SimulateTaskInfo taskInfo) {
        // 构建仿真任务信息
        int sceneId = taskInfo.getSceneId();
        SceneInfo sceneInfoById = this.sceneService.getSceneInfoById(sceneId);
        taskInfo.setField(sceneInfoById.getField());
        taskInfo.setCreater("current user");
        taskInfo.setCreateTime(cn.iecas.simulate.assessment.util.DateUtils.getVariableTime(new Date(), 8));
        taskInfo.setDelete(false);
        taskInfo.setStatus("WAIT");
        int insert = taskDao.insert(taskInfo);

        // 构建模型评估记录以及仿真统计信息
        List<Integer> assessmentIds = new ArrayList<>();
        List<Integer> modelIds = Arrays.stream(taskInfo.getModelId().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        for (Integer modelId : modelIds) {
            ModelAssessmentInfo assessmentInfo = new ModelAssessmentInfo();
            BeanUtils.copyProperties(taskInfo, assessmentInfo, "id", "modelId", "finishTime");
            TbModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
            assessmentInfo.setModelId(modelId);
            assessmentInfo.setModelName(modelInfo.getModelName());
            assessmentInfo.setUnit(modelInfo.getUnit());
            assessmentInfo.setTaskId(taskInfo.getId());
            this.modelAssessmentDao.insert(assessmentInfo);
            assessmentIds.add(assessmentInfo.getId());
        }

        AssessmentStatisticInfo statisticInfo = AssessmentStatisticInfo.builder()
                .modelAssessmentId(assessmentIds.toString().replace("[", "").replace("]", ""))
                .taskId(taskInfo.getId()).timeConsuming("0.0").simulateDataCount(0).avgImportNum(0.0).frequencyAdjustNum(0)
                .importFrequency(0.0).callCount(0).build();
        this.statisticDao.insert(statisticInfo);

        return taskInfo;
    }


   /**
    * @Description 更新仿真任务信息
    * @auther getao
    * @Date 2024/8/30 11:10
    * @Param [taskInfo]
    * @Return
    */
    @Override
    public SimulateTaskInfo updateSimulateTaskInfo(SimulateTaskInfo taskInfo) {
        LambdaUpdateChainWrapper<SimulateTaskInfo> update = new LambdaUpdateChainWrapper<>(this.taskDao);
        if (taskInfo.getId() != -1) {
            boolean flag = update.eq(SimulateTaskInfo::getId, taskInfo.getId())
                    .set(taskInfo.getTaskName() != null, SimulateTaskInfo::getTaskName, taskInfo.getTaskName())
                    .set(taskInfo.getDescribe() != null, SimulateTaskInfo::getDescribe, taskInfo.getDescribe())
                    .update();
        }
        return taskInfo;
    }


   /**
    * @Description 批量删除仿真任务信息
    * @auther getao
    * @Date 2024/8/30 11:11
    * @Param [idList]
    * @Return java.lang.Integer
    */
    @Override
    public Integer batchDeleteSimulateTask(List<Integer> idList) {
        int delete = this.taskDao.deleteBatchIds(idList);
        return delete;
    }


   /**
    * @Description 从模型提供方接入仿真数据
    * @auther getao
    * @Date 2024/8/30 11:11
    * @Param [taskInfoDto]
    * @Return
    */
    @Override
    @Transactional
    public List<SimulateDataInfo> getSimulateData(SimulateTaskInfoDto taskInfoDto) {
        int taskId = taskInfoDto.getId();
        int modelId = Integer.parseInt(taskInfoDto.getModelId());

        // TODO getao 28所模型数据引入接口
        //JSONObject simulateData = this.templateApi.getSimulateData(taskInfoDto);
        //List<SimulateDataInfo> dataInfos = simulateData.getJSONObject("data").getJSONArray("dataList").toJavaList(SimulateDataInfo.class);

        // TODO getao 模拟从模型获取引接数据 start
        SimulateDataInfoDto dataInfoDto = new SimulateDataInfoDto();
        dataInfoDto.setModelId(modelId);
        dataInfoDto.setTaskId(taskId);
        dataInfoDto.setPageNo(taskInfoDto.getPageNo());
        dataInfoDto.setPageSize(taskInfoDto.getPageSize());
        PageResult<SimulateDataInfo> dataInfo = dataService.listSimulateData(dataInfoDto);
        List<SimulateDataInfo> dataInfos = dataInfo.getResult();
        // end

        dataInfos.stream().map(e -> {
            e.setModelId(Integer.parseInt(taskInfoDto.getModelId()));
            e.setTaskId(taskInfoDto.getId());
            e.setImportTime(DateUtils.nowDate());
            return e;
        });
        boolean insert = this.dataService.insertBatch(dataInfos);

        // 更新仿真任务数据仿真消耗时间、总条数、平均引接数
        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        QueryWrapper<AssessmentStatisticInfo> statisticInfoWra = new QueryWrapper<>();
        statisticInfoWra.eq("task_id", taskId);
        AssessmentStatisticInfo statisticInfo = this.statisticDao.selectOne(statisticInfoWra);
        Date createTime = taskInfo.getCreateTime();
        long consumTime = System.currentTimeMillis() - createTime.getTime();
        String consumTimeStr = cn.iecas.simulate.assessment.util.DateUtils.millisToTime(consumTime);
        statisticInfo.setTimeConsuming(consumTimeStr);
        int dataCount = statisticInfo.getSimulateDataCount()+dataInfos.size();
        statisticInfo.setSimulateDataCount(dataCount);
        double avgImportNum = new BigDecimal(dataCount / (consumTime / 1000.0))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        statisticInfo.setAvgImportNum(avgImportNum);
        statisticInfo.setCallCount(statisticInfo.getCallCount()+1);
        double importFrequency = new BigDecimal(statisticInfo.getCallCount() / (consumTime / (1000.0 * 60)))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        statisticInfo.setImportFrequency(importFrequency);
        this.statisticDao.updateById(statisticInfo);

        return dataInfos;
    }


   /**
    * @Description 根据仿真任务id获取仿真数据以及运行统计信息
    * @auther getao
    * @Date 2024/8/30 11:12
    * @Param [taskId]
    * @Return
    */
    @Override
    public AssessmentStatisticInfo getSimulateTaskStsc(int taskId) {
        QueryWrapper<AssessmentStatisticInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        AssessmentStatisticInfo statisticInfo = null;

        try {
            statisticInfo = this.statisticDao.selectOne(wrapper);
        } catch (Exception e) {
            log.error("查询仿真任务统计信息执行失败，接口名 {} ，参数 {}：", "getSimulateTaskStsc", taskId);
            e.printStackTrace();
        }
        return statisticInfo;
    }


   /**
    * @Description 根据仿真任务id获取仿真任务信息
    * @auther getao
    * @Date 2024/8/30 11:13
    * @Param [taskInfoDto]
    * @Return
    */
    @Override
    public SimulateTaskInfo getSimulateTaskInfoById(SimulateTaskInfoDto taskInfoDto) {
        int taskId = taskInfoDto.getId();
        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);

        Date createTime = taskInfo.getCreateTime();
        long consumTime = System.currentTimeMillis() - createTime.getTime();
        String consumTimeStr = cn.iecas.simulate.assessment.util.DateUtils.millisToTime(consumTime);
        QueryWrapper<AssessmentStatisticInfo> statisticInfoWra = new QueryWrapper<>();
        statisticInfoWra.eq("task_id", taskId);
        AssessmentStatisticInfo statisticInfo = this.statisticDao.selectOne(statisticInfoWra);
        statisticInfo.setTimeConsuming(consumTimeStr);
        this.statisticDao.updateById(statisticInfo);

        return taskInfo;
    }


   /**
    * @Description 根据仿真任务id获取模型仿真数据评估信息
    * @auther getao
    * @Date 2024/8/30 11:14
    * @Param [taskId]
    * @Return org.json.JSONObject
    */
    @Override
    public JSONObject getModelAssessmentInfo(int taskId) {
        JSONObject assessmentResult = new JSONObject();

        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        String modelIds = taskInfo.getModelId();
        String weight = taskInfo.getModelWeight();
        String indexSystemIds = taskInfo.getIndexSystemId();
        List<Integer> indexSystemList = Arrays.stream(indexSystemIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> weightList = Arrays.stream(weight.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        // 指标评估
        for (Integer modelId : modelIdList) {
            TbModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
            // 获取任务对应该模型的仿真数据
            AssessmentResultInfo resultInfo = new AssessmentResultInfo();
            List<SimulateDataInfo> simulateDatas = this.dataService.getSimulateDataByModel(taskId, modelId);
            if (simulateDatas.size() == 0) {
                assessmentResult.put(taskId + "-" + modelId, resultInfo);
                continue;
            }
            int dataWeight = weightList.get(modelIdList.indexOf(modelId));
            List<SimulateDataInfo> assessmentDatas = CollectionsUtils.getListByWeight(simulateDatas, dataWeight);

            // 多模型模型评估逻辑
            resultInfo.setWeight(dataWeight);
            int indexSystemId = indexSystemList.get(modelIdList.indexOf(modelId));
            if (modelInfo.getSign().contains("FHGXFX")) {
                resultInfo = analysisService.getFHGXFXAssessmentInfo(assessmentDatas, indexSystemId, resultInfo);
            }
            if (modelInfo.getSign().contains("ZDRWLLFX")) {
                resultInfo = analysisService.getFHGXFXAssessmentInfo(assessmentDatas, indexSystemId, resultInfo);
            }
            assessmentResult.put(taskId + "-" + modelId, resultInfo);
        }

        // 体系贡献率评估
        this.contibutionAssessment(assessmentResult);

        return assessmentResult;
    }


   /**
    * @Description 获取体系贡献率评估结果
    * @auther getao
    * @Date 2024/9/4 17:01
    * @Param [assessmentResult]
    * @Return
    */
    private JSONObject contibutionAssessment(JSONObject assessmentResult) {
        double totalScore = 0;
        Set<String> keys = assessmentResult.keySet();
        for (String key : keys) {
            JSONObject assessmentInfo = assessmentResult.getJSONObject(key);
            Double score = assessmentInfo.getDouble("score") == null ? 0 : assessmentInfo.getDouble("score");
            int weight = assessmentInfo.getInteger("weight");
            totalScore += score * (weight / 100.0);
        }
        for (String key : keys) {
            JSONObject assessmentInfo = assessmentResult.getJSONObject(key);
            double score = assessmentInfo.getDouble("score") == null ? 0 : assessmentInfo.getDouble("score");
            if (Double.isNaN(totalScore) || totalScore == 0) {
                assessmentInfo.put("contibution", 0);
                continue;
            }
            int weight = assessmentInfo.getInteger("weight");
            double weightScore = score * (weight / 100.0);
            BigDecimal contibution = new BigDecimal(weightScore / totalScore * 100).setScale(2, RoundingMode.HALF_UP);
            assessmentInfo.put("contibution", contibution);
            assessmentResult.put(key, assessmentInfo);
        }
        return assessmentResult;
    }


    /**
     * @Description 根据导调指令，更新模型记录以及仿真任务状态
     * @auther getao
     * @Date 2024/9/9 11:36
     * @Param [op]
     * @Return void
     */
    @Override
    @Transactional
    public void updateTaskStatus(int taskId, int modelId, String op) {
        UpdateWrapper<SimulateTaskInfo> taskWrapper = new UpdateWrapper<>();
        taskWrapper.eq("id", taskId);
        LambdaUpdateChainWrapper<ModelAssessmentInfo> modelAsmWrapper = new LambdaUpdateChainWrapper<>(this.modelAssessmentDao);
        modelAsmWrapper.eq(ModelAssessmentInfo::getTaskId, taskId).eq(ModelAssessmentInfo::getModelId, modelId);
        if (op.equalsIgnoreCase("START")) {
            taskWrapper.set("status", "RUN");
            modelAsmWrapper.set(ModelAssessmentInfo::getStatus, "RUN");
        } else if (op.equalsIgnoreCase("PAUSE")) {
            taskWrapper.set("status", "PAUSE");
            modelAsmWrapper.set(ModelAssessmentInfo::getStatus, "PAUSE");
        } else if (op.equalsIgnoreCase("END")) {
            taskWrapper.set("status", "END");
            modelAsmWrapper.set(ModelAssessmentInfo::getStatus, "END");
        } else if (op.equalsIgnoreCase("CANCEL")) {
            taskWrapper.set("status", "CANCEL");
            modelAsmWrapper.set(ModelAssessmentInfo::getStatus, "CANCEL");
        } else if (op.equalsIgnoreCase("ASSESSMENT")) {
            taskWrapper.set("status", "ASSESSMENT");
            modelAsmWrapper.set(ModelAssessmentInfo::getStatus, "ASSESSMENT");
        }
        this.update(taskWrapper);
        modelAsmWrapper.update();
    }
}
