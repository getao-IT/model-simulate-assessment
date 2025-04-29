package cn.iecas.simulate.assessment.service.impl;

import cn.aircas.utils.date.DateUtils;
import cn.iecas.simulate.assessment.common.exception.CommonException;
import cn.iecas.simulate.assessment.dao.*;
import cn.iecas.simulate.assessment.dao.model.IndexIndicatorTaskDao;
import cn.iecas.simulate.assessment.dao.model.ZbCompareDao;
import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.*;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import cn.iecas.simulate.assessment.entity.model.domain.IndexIndicatorTaskInfo;
import cn.iecas.simulate.assessment.entity.model.domain.ZbCompareInfo;
import cn.iecas.simulate.assessment.service.*;
import cn.iecas.simulate.assessment.service.model.AssessmentService;
import cn.iecas.simulate.assessment.service.model.ModelTypeService;
import cn.iecas.simulate.assessment.service.model.SimulateDataService;
import cn.iecas.simulate.assessment.service.model.impl.ModelCommonServiceImpl;
import cn.iecas.simulate.assessment.util.CollectionsUtils;
import cn.iecas.simulate.assessment.util.JSONUtils;
import cn.iecas.simulate.assessment.util.MathUtils;
import cn.iecas.simulate.assessment.util.UserUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;



/**
 * @auther getao
 * @Date 2024/8/23 9:12
 * @Description 仿真任务服务接口实现类
 */
@Slf4j
@Service
public class SimulateTaskServiceImpl extends ServiceImpl<SimulateTaskDao, SimulateTaskInfo> implements SimulateTaskService {

    @Value(value = "${assessment.detection.defaultSamplePath}")
    private String defaultSamplePath;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private SimulateTaskDao taskDao;

    @Autowired
    private AssessmentStatisticDao statisticDao;

    @Autowired
    private ModelAssessmentDao modelAssessmentDao;

    @Autowired
    private AssessmentProcessDao processDao;

    @Autowired
    private ModelShareDao shareDao;

    @Autowired
    private SimulateDataDao simulateDataDao;

    @Autowired
    private IndexIndicatorTaskDao indicatorTaskDao;

    @Autowired
    private ZbCompareDao compareDao;

    @Autowired
    private ModelService modelService;

    @Autowired
    private SceneService sceneService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private ModelCommonServiceImpl commonService;

    @Autowired
    private IndexInfoService indexInfoService;

    @Autowired
    private IndexSystemService indexSystemService;

    @Autowired
    private AssessmentResultService resultService;

    @Autowired
    private ModelRunDataService runDataService;

    @Autowired
    private ModelAssessmentService assessmentService;


    /**
     * @Description 分页获取仿真任务信息
     * @auther getao
     * @Date 2024/8/30 11:09
     * @Param [taskInfoDto]
     * @Return
     */
    @Override
    public PageResult<SimulateTaskInfo> getSimulateTaskInfo(SimulateTaskInfoDto taskInfoDto) {
        int userId = userUtils.getUserIdByToken();
        JSONObject userInfo = userUtils.getUserJsonInfoByToken();
        Boolean isAdmin = userInfo.getBoolean("is_admin");
        Boolean isSuperAdmin = userInfo.getBoolean("is_super_admin");
        IPage<SimulateTaskInfo> page = new Page<>(taskInfoDto.getPageNo(), taskInfoDto.getPageSize());
        QueryWrapper<SimulateTaskInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("delete", false)
                .eq(taskInfoDto.getId() != null, "id", taskInfoDto.getId())
                .like(taskInfoDto.getTaskName() != null, "task_name", taskInfoDto.getTaskName())
                .eq(taskInfoDto.getTaskType() != null, "task_type", taskInfoDto.getTaskType())
                .like(taskInfoDto.getUserLevel() != null, "user_level", taskInfoDto.getUserLevel())
                .eq(taskInfoDto.getField() != null, "field", taskInfoDto.getField())
                .like(taskInfoDto.getSceneName() != null, "scene_name", taskInfoDto.getSceneName())
                .like(taskInfoDto.getModelName() != null, "model_name", taskInfoDto.getModelName())
                .like(taskInfoDto.getCreater() != null, "creater", taskInfoDto.getCreater())
                .le(taskInfoDto.getLeTime() != null, "create_time", taskInfoDto.getLeTime())
                .ge(taskInfoDto.getGeTime() != null, "create_time", taskInfoDto.getGeTime())
                .like(taskInfoDto.getFuzzy() != null, "CONCAT(task_name, user_level, scene_name, model_name" +
                        ",creater, describe)", taskInfoDto.getFuzzy())
                .orderByDesc(taskInfoDto.getOrderCol() == null, "id")
                .orderByDesc(taskInfoDto.getOrderCol() != null
                        && taskInfoDto.getOrderWay().equalsIgnoreCase("desc"), taskInfoDto.getOrderCol())
                .orderByAsc(taskInfoDto.getOrderCol() != null
                        && taskInfoDto.getOrderWay().equalsIgnoreCase("asc"), taskInfoDto.getOrderCol());
        if (!isAdmin && !isSuperAdmin) {
            wrapper.eq("user_id", userId);
        }
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
        CommonResult<JSONObject> userResult = userUtils.getUserInfoByToken(userUtils.getUserToken());
        if (userResult.getCode().equalsIgnoreCase("0")) {
            JSONObject userInfo = userResult.getData();
            taskInfo.setUserId(userInfo.getInteger("id"));
            taskInfo.setCreater(userInfo.getString("name"));
        } else {
            taskInfo.setUserId(-1);
            taskInfo.setCreater("current user");
        }
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
            ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
            assessmentInfo.setModelId(modelId);
            assessmentInfo.setModelName(modelInfo.getModelName());
            assessmentInfo.setUnit(modelInfo.getUnit());
            assessmentInfo.setTaskId(taskInfo.getId());
            assessmentInfo.setRegisterScore(modelInfo.getRegisterScore());
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
    @Transactional
    public SimulateTaskInfo updateSimulateTaskInfo(SimulateTaskInfo taskInfo) {
        LambdaUpdateChainWrapper<SimulateTaskInfo> update = new LambdaUpdateChainWrapper<>(this.taskDao);
        if (taskInfo.getId() != -1) {
            if (taskInfo.getModelId() != null) {
                //this.modelAssessmentService.deleteHistoryByTaskId(taskInfo.getId());
                SimulateTaskInfo targetTask = this.taskDao.selectById(taskInfo.getId());
                List<Integer> assessmentIds = new ArrayList<>();
                List<Integer> modelIds = Arrays.stream(taskInfo.getModelId().split(",")).map(Integer::parseInt).collect(Collectors.toList());
                for (Integer modelId : modelIds) {
                    ModelAssessmentInfo assessmentInfo = new ModelAssessmentInfo();
                    BeanUtils.copyProperties(targetTask, assessmentInfo, "id", "modelId", "finishTime");
                    ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
                    assessmentInfo.setModelId(modelId);
                    assessmentInfo.setModelName(modelInfo.getModelName());
                    assessmentInfo.setUnit(modelInfo.getUnit());
                    assessmentInfo.setTaskId(taskInfo.getId());
                    this.modelAssessmentDao.insert(assessmentInfo);
                    assessmentIds.add(assessmentInfo.getId());
                }
                QueryWrapper<AssessmentStatisticInfo> sttsWrapper = new QueryWrapper<>();
                sttsWrapper.eq("task_id", taskInfo.getId());
                AssessmentStatisticInfo assessmentStatisticInfo = this.statisticDao.selectList(sttsWrapper).stream().findFirst().get();
                String modelAssessId = assessmentIds.toString().replace("[", "").replace("]", "");
                assessmentStatisticInfo.setModelAssessmentId(modelAssessId);
                this.statisticDao.updateById(assessmentStatisticInfo);
                update.set(SimulateTaskInfo::getModelId, taskInfo.getModelId()).set(SimulateTaskInfo::getModelName,
                        taskInfo.getModelName()).set(SimulateTaskInfo::getModelWeight, taskInfo.getModelWeight());
            }

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
    @Transactional
    public Integer batchDeleteSimulateTask(List<Integer> idList) {
        int delete = this.taskDao.deleteBatchIds(idList);

        QueryWrapper<ModelAssessmentInfo> maiWrapper = new QueryWrapper<>();
        maiWrapper.in("task_id", idList);
        this.modelAssessmentDao.delete(maiWrapper);

        QueryWrapper<AssessmentStatisticInfo> mastscWrapper = new QueryWrapper<>();
        mastscWrapper.in("task_id", idList);
        this.statisticDao.delete(mastscWrapper);

        QueryWrapper<SimulateDataInfo> simuDataWrapper = new QueryWrapper<>();
        simuDataWrapper.in("task_id", idList);
        this.simulateDataDao.delete(simuDataWrapper);

        QueryWrapper<AssessmentProcessInfo> processWrapper = new QueryWrapper<>();
        processWrapper.in("task_id", idList);
        this.processDao.delete(processWrapper);

        QueryWrapper<ModelShareInfo> shareWrapper = new QueryWrapper<>();
        shareWrapper.in("task_id", idList);
        this.shareDao.delete(shareWrapper);

        QueryWrapper<IndexIndicatorTaskInfo> indicWrapper = new QueryWrapper<>();
        indicWrapper.in("task_id", idList);
        this.indicatorTaskDao.delete(indicWrapper);

        QueryWrapper<ZbCompareInfo> zbWrapper = new QueryWrapper<>();
        zbWrapper.in("task_id", idList);
        this.compareDao.delete(zbWrapper);

        QueryWrapper<ModelRunlDataInfo> rdWrapper = new QueryWrapper<>();
        rdWrapper.in("task_id", idList);
        this.runDataService.remove(rdWrapper);

        QueryWrapper<AssessmentResultInfo> asmRsWrapper = new QueryWrapper<>();
        asmRsWrapper.in("task_id", idList);
        this.resultService.remove(asmRsWrapper);

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

        //  28所模型数据引入接口
        //JSONObject simulateData = this.templateApi.getSimulateData(taskInfoDto);
        //List<SimulateDataInfo> dataInfos = simulateData.getJSONObject("data").getJSONArray("dataList").toJavaList(SimulateDataInfo.class);

        //  模拟从模型获取引接数据 start
        SimulateDataInfoDto dataInfoDto = new SimulateDataInfoDto();
        dataInfoDto.setModelId(modelId);
        dataInfoDto.setTaskId(taskId);
        dataInfoDto.setPageNo(taskInfoDto.getPageNo());
        dataInfoDto.setPageSize(taskInfoDto.getPageSize());
        SimulateDataService dataService = this.commonService.getDataServiceFromModel(modelId);
        PageResult<SimulateDataInfo> dataInfo = dataService.listSimulateData(dataInfoDto);
        List<SimulateDataInfo> dataInfos = dataInfo.getResult();
        // end

        dataInfos.stream().map(e -> {
            e.setModelId(Integer.parseInt(taskInfoDto.getModelId()));
            e.setTaskId(taskInfoDto.getId());
            e.setImportTime(DateUtils.nowDate());
            return e;
        });
        boolean insert = dataService.insertBatch(dataInfos);

        // 更新仿真任务数据仿真消耗时间、总条数、平均引接数
        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        QueryWrapper<AssessmentStatisticInfo> statisticInfoWra = new QueryWrapper<>();
        statisticInfoWra.eq("task_id", taskId);
        AssessmentStatisticInfo statisticInfo = this.statisticDao.selectOne(statisticInfoWra);
        Date createTime = taskInfo.getCreateTime();
        long consumTime = System.currentTimeMillis() - createTime.getTime();
        String consumTimeStr = cn.iecas.simulate.assessment.util.DateUtils.millisToTime(consumTime);
        statisticInfo.setTimeConsuming(consumTimeStr);
        int dataCount = statisticInfo.getSimulateDataCount() + dataInfos.size();
        statisticInfo.setSimulateDataCount(dataCount);
        double avgImportNum = new BigDecimal(dataCount / (consumTime / 1000.0))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        statisticInfo.setAvgImportNum(avgImportNum);
        statisticInfo.setCallCount(statisticInfo.getCallCount() + 1);
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
    public JSONArray getModelAssessmentInfo(int taskId) {
        JSONArray assessmentResult = new JSONArray();

        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        String modelIds = taskInfo.getModelId();
        String weight = taskInfo.getModelWeight();
        String indexSystemIds = taskInfo.getIndexSystemId();
        List<Integer> indexSystemList = Arrays.stream(indexSystemIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> weightList = Arrays.stream(weight.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        // 指标评估
        for (Integer modelId : modelIdList) {
            JSONObject modelAssessment = new JSONObject();
            ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
            // 获取任务对应该模型的仿真数据
            IndexResultInfo resultInfo = new IndexResultInfo();
            resultInfo.setModelId(modelId);
            resultInfo.setTaskId(taskId);
            modelAssessment.put("modelId", modelId);
            modelAssessment.put("name", modelInfo.getModelName() + "评估结果");
            SimulateDataService dataService = this.commonService.getDataServiceFromModel(modelId);
            List<SimulateDataInfo> simulateDatas = dataService.getSimulateDataByModel(taskId, modelId);
            if (simulateDatas.size() == 0) {
                modelAssessment.put("value", resultInfo);
                assessmentResult.add(modelAssessment);
                continue;
            }
            int dataWeight = weightList.get(modelIdList.indexOf(modelId));
            List<SimulateDataInfo> assessmentDatas = CollectionsUtils.getListByWeight(simulateDatas, dataWeight);

            // 多模型模型评估逻辑
            resultInfo.setWeight(dataWeight);
            int indexSystemId = indexSystemList.get(modelIdList.indexOf(modelId));
            AssessmentService serviceFromModel = commonService.getAnalysisServiceFromModel(modelId);
            serviceFromModel.getModelAssessmentInfo(assessmentDatas, indexSystemId, resultInfo, taskId);

            modelAssessment.put("value", resultInfo);
            assessmentResult.add(modelAssessment);
            //assessmentResult.put(taskId + "-" + modelId, resultInfo);
        }

        // 体系贡献率评估
        this.contibutionAssessment(assessmentResult);

        return assessmentResult;
    }


    /**
     * @Description 更新模型配置信息
     * @Author getao
     * @Date 12:28 2025/3/22
     * @Param [modelId]
     * @return cn.iecas.simulate.assessment.entity.domain.ModelInfo
     */
    @Override
    public ModelInfo updateModelConfig(ModelInfo modelInfo) {
        if (modelInfo.getAssessmentUrl() != null) {
            JSONObject assessmentUrl = modelInfo.getAssessmentUrl();
            String samplePath = this.defaultSamplePath + assessmentUrl.getString("sample") + "/xmls";
            assessmentUrl.put("sample", samplePath);
            modelInfo.setAssessmentUrl(assessmentUrl);
        }
        UpdateWrapper<ModelInfo> update = new UpdateWrapper<>();
        update.set(modelInfo.getRunUrl() != null, "run_url", modelInfo.getRunUrl())
                .set(modelInfo.getAssessmentUrl() != null, "assessment_url", modelInfo.getAssessmentUrl())
                .eq("id", modelInfo.getId());
        this.modelService.update(update);
        return modelInfo;
    }

    /**
     * @Description 根据仿真任务id获取模型仿真数据评估信息
     * @auther getao
     * @Date 2024/8/30 11:14
     * @Param [taskId]
     * @Return org.json.JSONObject
     */
    @Override
    public JSONArray getModelAssessmentInfoNew(int taskId) {
        QueryWrapper<AssessmentResultInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        List<AssessmentResultInfo> list = this.resultService.list(wrapper);
        if (list == null || list.size() == 0) {
            JSONArray rs = new JSONArray();
            JSONObject e = new JSONObject();
            e.put("modelId", 0);
            e.put("name", "无评估信息");
            e.put("value", new JSONObject());
            e.put("contibution", 0);
            e.put("weight", 0);
            rs.add(e);
            return rs;
        }

        SimulateTaskInfo taskInfo = this.getById(taskId);
        String modelIds = taskInfo.getModelId();
        String weights = taskInfo.getModelWeight();
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> weightsList = Arrays.stream(weights.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        JSONArray assessmentResult = new JSONArray();
        try {
            StringJoiner names = new StringJoiner("-");
            Map<Integer, List<AssessmentResultInfo>> asmRsByModel = list.stream().collect(Collectors.groupingBy(AssessmentResultInfo::getModelId));
            for (Map.Entry<Integer, List<AssessmentResultInfo>> entry : asmRsByModel.entrySet()) {
                Integer modelId = entry.getKey();
                List<AssessmentResultInfo> asmByModel = entry.getValue();
                ModelInfo modelInfo = this.modelService.getById(modelId);
                JSONObject rs = new JSONObject();

                List<String> values = asmByModel.stream().map(AssessmentResultInfo::getValue).collect(Collectors.toList());
                JSONObject avgValue = JSONObject.parseObject(JSONUtils.mergeJsonStrAndCalculate(values));
                if (!avgValue.containsKey("sorce"))
                    avgValue.put("sorce", 0);
                Integer weight = weightsList.get(modelIdList.indexOf(modelId));
                avgValue.put("contibution", weight);
                rs.put("modelId", modelId);
                rs.put("name", modelInfo.getModelName());
                rs.put("value", avgValue);
                rs.put("contibution", weight);
                rs.put("weight", weight);

                names.add(modelInfo.getModelName());
                assessmentResult.add(rs);
            }

            if (asmRsByModel.size() > 1) {
                List<JSONObject> overAllAsm = assessmentResult.toJavaList(JSONObject.class);
                JSONObject allAsmAvgValue = JSONObject.parseObject(JSONUtils.mergeJsonAndCalculate(overAllAsm));
                allAsmAvgValue.put("contibution", 100);
                JSONObject rs = new JSONObject();
                rs.put("modelId", 0);
                rs.put("name", names.toString());
                rs.put("value", allAsmAvgValue);
                rs.put("contibution", 100);
                rs.put("weight", 100);
                assessmentResult.add(rs);
            }

            // 体系贡献率评估
            //this.contibutionAssessment(assessmentResult);
        } catch (JsonProcessingException jsonProcessingException) {
            jsonProcessingException.printStackTrace();
        }

        return assessmentResult;
    }


    /**
     * @Description 获取体系贡献率评估结果
     * @auther getao
     * @Date 2024/9/4 17:01
     * @Param [assessmentResult]
     * @Return
     */
    private JSONArray contibutionAssessment(JSONArray assessmentResultArr) {
        double totalScore = 0;

        for (Object o : assessmentResultArr) {
            JSONObject assessmentResult = JSON.parseObject(JSON.toJSONString(o));
            JSONObject assessmentInfo = assessmentResult.getJSONObject("value");
            Double score = assessmentInfo.getDouble("score") == null ? 0 : assessmentInfo.getDouble("score");
            int weight = assessmentInfo.getInteger("weight");
            totalScore += score * (weight / 100.0);
        }

        for (Object o : assessmentResultArr) {
            JSONObject assessmentResult = JSON.parseObject(JSON.toJSONString(o));
            JSONObject assessmentInfo = assessmentResult.getJSONObject("value");
            double score = assessmentInfo.getDouble("score") == null ? 0 : assessmentInfo.getDouble("score");
            if (Double.isNaN(totalScore) || totalScore == 0) {
                assessmentInfo.put("contibution", 0);
                continue;
            }
            int weight = assessmentInfo.getInteger("weight");
            double weightScore = score * (weight / 100.0);
            BigDecimal contibution = new BigDecimal(weightScore / totalScore * 100).setScale(2, RoundingMode.HALF_UP);
            assessmentInfo.put("contibution", contibution);
            assessmentResult.put("value", assessmentInfo);
        }
        return assessmentResultArr;
    }


    /**
     * @author: getao
     * @Date: 2024/9/26 16:08
     * @Description: 导出模型评估报告
     */
    @Override
    public void exportAssessmentReport(int taskId, int modelId, double contibution) {
        // TODO getao 未完成的接口，后续可优化为该种形式
        /*ModelTypeService modelTypeService = this.commonService.getModelTypeService(modelId);
        modelTypeService.exportAssessmentReport(taskId, modelId, contibution);*/

        ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        if (modelId == 0 || modelInfo.getModelType().equalsIgnoreCase("detection")) {
            this.exprotReportFromDct(modelInfo, taskId, modelId, contibution);
        } else {
            this.exprotReportFromAnalysis(modelInfo, taskId, modelId, contibution);
        }
    }


    /**
     * 导出目标价检测类模型的评估报告
     *
     * @param modelInfo
     * @param taskId
     * @param modelId
     * @param contibution
     */
    private void exprotReportFromDct(ModelInfo modelInfo, int taskId, int modelId, double contibution) {
        JSONArray assessmentInfos = this.getModelAssessmentInfoNew(taskId);
        IndexResultInfo resultInfo = new IndexResultInfo();
        for (Object assessmentInfo : assessmentInfos) {
            JSONObject object = JSON.parseObject(JSONObject.toJSONString(assessmentInfo));
            Integer targetModelId = object.getInteger("modelId");
            if (modelId == targetModelId) {
                resultInfo = object.getJSONObject("value").toJavaObject(IndexResultInfo.class);
            }
        }

        if (modelInfo == null) {
            modelInfo = new ModelInfo();
            modelInfo.setModelName("统一多模型分析");
        }

        try {
            // 第一步，实例化一个document对象
            Document document = new Document();
            // 第二步，设置要到出的路径
            //FileOutputStream out = new FileOutputStream("C:\\Users\\Administrator\\Downloads\\exportPdf\\" + modelInfo.getModelName() + "_模型评估报告" + "_" + System.currentTimeMillis() + ".pdf");
            //如果是浏览器通过request请求需要在浏览器中输出则使用下面方式
            OutputStream out = response.getOutputStream();
            // 第三步,设置字符
            BaseFont stFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(stFont, 16.0F, Font.BOLD);
            Font tableDescriptFont = new Font(stFont, 10.0F, Font.NORMAL);
            Font tableHeaderFont = new Font(stFont, 12.0F, Font.BOLD);
            Font totalScoreFont = new Font(stFont, 14.0F, Font.BOLD);
            Font contentFont = new Font(stFont, 12.0F, Font.NORMAL);
            // 第四步，将pdf文件输出到磁盘
            PdfWriter writer = PdfWriter.getInstance(document, out);
            // 第五步，打开生成的pdf文件
            document.open();
            // 第六步,设置内容
            // 设置标题
            String title = modelInfo.getModelName() + "评估报告";
            Paragraph titlePag = new Paragraph(new Chunk(title, titleFont));
            titlePag.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePag);
            document.add(new Paragraph("\n"));
            // 设置表格描述
            Paragraph modelName = new Paragraph(new Chunk("模型名称：" + modelInfo.getModelName(), tableDescriptFont));
            document.add(modelName);
            Paragraph assessmentTime = new Paragraph(new Chunk("评估时间：" + DateUtils.nowDate(), tableDescriptFont));
            document.add(assessmentTime);
            document.add(new Paragraph("\n"));

            // 总得分
            double totalScore = resultInfo.getScore();
            Paragraph totalScorePar = new Paragraph(new Chunk("模型评估得分：" + totalScore, totalScoreFont));
            document.add(totalScorePar);
            document.add(new Paragraph("\n"));

            // 模型评估分析表
            Paragraph modelAnalyseTitle = new Paragraph(new Chunk("模型评估分析表", tableHeaderFont));
            document.add(modelAnalyseTitle);
            document.add(new Paragraph("\n"));

            // 通用指标表格
            JSONArray firstIndex = resultInfo.getFirstIndex();
            PdfPTable firstTable = this.getModelAssessmentFirstTable(firstIndex, tableHeaderFont, contentFont, 5);
            document.add(firstTable);
            document.add(new Paragraph("\n"));

            // 其他指标表格
            PdfPTable otherTable = this.getModelAssessmentOtherTable(resultInfo, modelInfo.getModelName(), tableHeaderFont, contentFont, 6);
            document.add(otherTable);
            document.add(new Paragraph("\n"));

            // 评估结论描述信息
            Paragraph modelAssessmentResultTitle = new Paragraph(new Chunk("模型评估结论", tableHeaderFont));
            String assessmentConclusion = this.getAssessmentConclusion(resultInfo, modelInfo);
            Paragraph modelAssessmentResult = new Paragraph(new Chunk(assessmentConclusion, contentFont));
            modelAssessmentResult.setFirstLineIndent(10);
            document.add(modelAssessmentResultTitle);
            document.add(new Paragraph("\n"));
            document.add(modelAssessmentResult);
            document.add(new Paragraph("\n"));

            // 模型评估参考值表
            Paragraph modelAssessmentRefTitle = new Paragraph(new Chunk("模型评估参考表", tableHeaderFont));
            PdfPTable assessmentRefTable = this.getModelAssessmentRefTable(tableHeaderFont, contentFont, 9);
            document.add(modelAssessmentRefTitle);
            document.add(new Paragraph("\n"));
            document.add(assessmentRefTable);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("备注：该评估结果仅代表本次模型仿真数据评估结论，不代表模型实际应用能力。", tableDescriptFont));

            // 第七步，关闭document
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 导出数据处理类模型的评估报告
     *
     * @param modelInfo
     * @param taskId
     * @param modelId
     * @param contibution
     */
    private void exprotReportFromAnalysis(ModelInfo modelInfo, int taskId, int modelId, double contibution) {
        JSONObject assessmentResult = new JSONObject();
        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        String modelIds = taskInfo.getModelId();
        String weight = taskInfo.getModelWeight();
        String indexSystemIds = taskInfo.getIndexSystemId();
        List<Integer> indexSystemList = Arrays.stream(indexSystemIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> weightList = Arrays.stream(weight.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        //// 指标评估
        // 获取任务对应该模型的仿真数据
        IndexResultInfo resultInfo = new IndexResultInfo();
        resultInfo.setTaskId(taskId);
        resultInfo.setModelId(modelId);
        SimulateDataService dataService = this.commonService.getDataServiceFromModel(modelId);
        List<SimulateDataInfo> simulateDatas = dataService.getSimulateDataByModel(taskId, modelId);
        if (simulateDatas.size() == 0) {
            assessmentResult.put(taskId + "-" + modelId, resultInfo);
            return;
        }
        int dataWeight = weightList.get(modelIdList.indexOf(modelId));
        List<SimulateDataInfo> assessmentDatas = CollectionsUtils.getListByWeight(simulateDatas, dataWeight);
        // 多模型模型评估逻辑
        resultInfo.setWeight(dataWeight);
        resultInfo.setContibution(contibution);
        int indexSystemId = indexSystemList.get(modelIdList.indexOf(modelId));
        AssessmentService serviceFromModel = commonService.getAnalysisServiceFromModel(modelId);
        serviceFromModel.getModelAssessmentInfo(assessmentDatas, indexSystemId, resultInfo, taskId);

        try {
            // 第一步，实例化一个document对象
            Document document = new Document();
            // 第二步，设置要到出的路径
            //FileOutputStream out = new FileOutputStream("C:\\Users\\Administrator\\Downloads\\exportPdf\\" + modelInfo.getModelName() + "_模型评估报告" + "_" + System.currentTimeMillis() + ".pdf");
            //如果是浏览器通过request请求需要在浏览器中输出则使用下面方式
            OutputStream out = response.getOutputStream();
            // 第三步,设置字符
            BaseFont stFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(stFont, 16.0F, Font.BOLD);
            Font tableDescriptFont = new Font(stFont, 10.0F, Font.NORMAL);
            Font tableHeaderFont = new Font(stFont, 12.0F, Font.BOLD);
            Font contentFont = new Font(stFont, 12.0F, Font.NORMAL);
            // 第四步，将pdf文件输出到磁盘
            PdfWriter writer = PdfWriter.getInstance(document, out);
            // 第五步，打开生成的pdf文件
            document.open();
            // 第六步,设置内容
            // 设置标题
            String title = modelInfo.getModelName() + "模型评估报告";
            Paragraph titlePag = new Paragraph(new Chunk(title, titleFont));
            titlePag.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePag);
            document.add(new Paragraph("\n"));
            // 设置表格描述
            Paragraph modelName = new Paragraph(new Chunk("模型名称：" + modelInfo.getModelName(), tableDescriptFont));
            document.add(modelName);
            Paragraph assessmentTime = new Paragraph(new Chunk("评估时间：" + DateUtils.nowDate(), tableDescriptFont));
            document.add(assessmentTime);
            document.add(new Paragraph("\n"));

            Paragraph modelAnalyseTitle = new Paragraph(new Chunk("模型评估分析表", tableHeaderFont));
            document.add(modelAnalyseTitle);
            document.add(new Paragraph("\n"));

            // 通用指标表格
            JSONArray firstIndex = resultInfo.getFirstIndex();
            PdfPTable firstTable = this.getModelAssessmentFirstTable(firstIndex, tableHeaderFont, contentFont, 5);
            document.add(firstTable);
            document.add(new Paragraph("\n"));

            // 其他指标表格
            PdfPTable otherTable = this.getModelAssessmentOtherTable(resultInfo, modelInfo.getModelName(), tableHeaderFont, contentFont, 6);
            document.add(otherTable);
            document.add(new Paragraph("\n"));

            // 评估结论描述信息
            Paragraph modelAssessmentResultTitle = new Paragraph(new Chunk("模型评估结论", tableHeaderFont));
            String assessmentConclusion = this.getAssessmentConclusion(resultInfo, modelInfo);
            Paragraph modelAssessmentResult = new Paragraph(new Chunk(assessmentConclusion, contentFont));
            modelAssessmentResult.setFirstLineIndent(10);
            document.add(modelAssessmentResultTitle);
            document.add(new Paragraph("\n"));
            document.add(modelAssessmentResult);
            document.add(new Paragraph("\n"));

            // 模型评估参考值表
            Paragraph modelAssessmentRefTitle = new Paragraph(new Chunk("模型评估参考表", tableHeaderFont));
            PdfPTable assessmentRefTable = this.getModelAssessmentRefTable(tableHeaderFont, contentFont, 9);
            document.add(modelAssessmentRefTitle);
            document.add(new Paragraph("\n"));
            document.add(assessmentRefTable);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("备注：该评估结果仅代表本次模型仿真数据评估结论，不代表模型实际应用能力。", tableDescriptFont));

            // 第七步，关闭document
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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


    /**
     * @author: getao
     * @Date: 2024/9/29 12:47
     * @Description: 获取某级别某指标名称包含的子指标行数
     */
    private int getRowSpanNumber(IndexResultInfo resultInfo, int indexLevel, String indexName) {
        int rowSpan = 0;
        JSONArray thireIndex = resultInfo.getThreeIndex();
        JSONArray fourIndex = resultInfo.getFourIndex();

        if (indexLevel == 3) {
            for (Object fourIdx : fourIndex) { // 四级指标
                JSONObject jsonFourIndex = (JSONObject) fourIdx;
                String fourParentIndex = jsonFourIndex.getString("parentIndex");
                if (fourParentIndex.equalsIgnoreCase(indexName)) {
                    JSONArray fourContents = jsonFourIndex.getJSONArray("contents");
                    rowSpan += fourContents.size();
                }
            }
        }

        if (indexLevel == 2) {
            for (Object thireIdx : thireIndex) { // 三级指标
                JSONObject jsonThireIndex = (JSONObject) thireIdx;
                String thireParentIndex = jsonThireIndex.getString("parentIndex");
                if (thireParentIndex.equalsIgnoreCase(indexName)) {
                    JSONArray thireContents = jsonThireIndex.getJSONArray("contents");
                    for (Object thireContent : thireContents) {
                        JSONObject thireElement = (JSONObject) thireContent;
                        String thireIndexName = thireElement.getString("name");
                        for (Object fourIdx : fourIndex) { // 四级指标
                            JSONObject jsonFourIndex = (JSONObject) fourIdx;
                            String fourParentIndex = jsonFourIndex.getString("parentIndex");
                            if (fourParentIndex.equalsIgnoreCase(thireIndexName)) {
                                JSONArray fourContents = jsonFourIndex.getJSONArray("contents");
                                rowSpan += fourContents.size();
                            }
                        }
                    }
                }
            }
        }
        return rowSpan;
    }


    /**
     * @author: getao
     * @Date: 2024/9/29 13:02
     * @Description: 设置单元格格式
     */
    private PdfPCell setPadPCellStyle(String content, Font font, int hAlignment, int vAlignment, int rowSpan, int colSpan) {
        PdfPCell cell = new PdfPCell();
        cell.setPhrase(new Paragraph(content, font));
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        cell.setHorizontalAlignment(hAlignment);
        cell.setVerticalAlignment(vAlignment);
        cell.setRowspan(rowSpan);
        cell.setColspan(colSpan);
        return cell;
    }


    /**
     * @author: getao
     * @Date: 2024/9/29 13:02
     * @Description: 设置单元格格式
     */
    private String getAssessmentConclusion(IndexResultInfo assessmentInfo, ModelInfo modelInfo) {
        String sceneName = "--";
        String modelName = "--";
        Double score = assessmentInfo.getScore();
        String scoreAssess = "";
        double weight = assessmentInfo.getWeight();
        Double contibution = assessmentInfo.getContibution();
        String contibutionAssess = "";

        SimulateTaskInfo simulateTaskInfo = this.getById(assessmentInfo.getTaskId());
        sceneName = simulateTaskInfo.getSceneName();
        modelName = modelInfo.getModelName();
        if (score < 60) {
            scoreAssess = "不及格，不符合模型指标体系的评估要求。";
        } else if (score >= 60 && score < 80) {
            scoreAssess = "及格，有必要进一步优化模型能力。";
        } else if (score >= 80 && score < 90) {
            scoreAssess = "良好， 根据实际需求，有必要进一步优化模型能力。";
        } else if (score >= 90 && score <= 100) {
            scoreAssess = "优秀，符合模型指标体系的评估要求。";
        }
        if (!simulateTaskInfo.getTaskType().equalsIgnoreCase("FZPG")) {
            contibutionAssess = "体系贡献率为" + contibution + "%，";
        }
        String content = "评估模型 ”" + modelName + "“，在场景 ”" + sceneName + "“ 下评估正确完成，其模型能力可用、仿真数据实时传输" +
                "、接口协议符合规则、数据格式符合规范，通过对模型的仿真数据分析，”" + modelName + "“ 在各个指标的表现情况如上表所示，其模型综合" +
                "评分为" + score + "分，" + contibutionAssess + "表现" + scoreAssess;

        return content;
    }


    /**
     * @author: getao
     * @Date: 2024/9/30 10:18
     * @Description: 获取其他指标评估分析表
     */
    public PdfPTable getModelAssessmentOtherTable(IndexResultInfo resultInfo, String modelName, Font tableHeaderFont,
                                                  Font contentFont, int colNumber) {
        PdfPTable otherTable = new PdfPTable(colNumber);
        otherTable.setWidthPercentage(100.0F);
        //第一列是列表名
        otherTable.setHeaderRows(1);
        otherTable.getDefaultCell().setHorizontalAlignment(1);
        // 表头
        PdfPCell otherTableTitle = this.setPadPCellStyle(modelName + "指标评估分析表",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 6);
        otherTable.addCell(otherTableTitle);
        otherTable.addCell(new Paragraph("二级指标", tableHeaderFont));
        otherTable.addCell(new Paragraph("得分", tableHeaderFont));
        otherTable.addCell(new Paragraph("三级指标", tableHeaderFont));
        otherTable.addCell(new Paragraph("得分", tableHeaderFont));
        otherTable.addCell(new Paragraph("四级指标", tableHeaderFont));
        otherTable.addCell(new Paragraph("得分", tableHeaderFont));
        // 表内容
        JSONArray secondIndex = resultInfo.getSecondIndex();
        JSONArray thireIndex = resultInfo.getThreeIndex();
        JSONArray fourIndex = resultInfo.getFourIndex();
        for (Object secondIdx : secondIndex) { // 二级指标
            JSONObject jsonSecondIndex = (JSONObject) secondIdx;
            JSONArray secondContents = jsonSecondIndex.getJSONArray("contents");
            for (Object secondContent : secondContents) {
                JSONObject secondElement = (JSONObject) secondContent;
                int secondRowSpan = getRowSpanNumber(resultInfo, 2, secondElement.getString("name"));
                PdfPCell secondIndexName = this.setPadPCellStyle(secondElement.getString("name"),
                        contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, secondRowSpan, 1);
                otherTable.addCell(secondIndexName);
                PdfPCell secondIndexscore = this.setPadPCellStyle(secondElement.getString("score"),
                        contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, secondRowSpan, 1);
                otherTable.addCell(secondIndexscore);
                for (Object thireIdx : thireIndex) { // 三级指标
                    JSONObject jsonThireIndex = (JSONObject) thireIdx;
                    String thireParentIndex = jsonThireIndex.getString("parentIndex");
                    if (thireParentIndex.equalsIgnoreCase(secondElement.getString("name"))) {
                        JSONArray thireContents = jsonThireIndex.getJSONArray("contents");
                        for (Object thireContent : thireContents) {
                            JSONObject thireElement = (JSONObject) thireContent;
                            int thireRowSpan = getRowSpanNumber(resultInfo, 3, thireElement.getString("name"));
                            PdfPCell thireIndexName = this.setPadPCellStyle(thireElement.getString("name"),
                                    contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, thireRowSpan, 1);
                            otherTable.addCell(thireIndexName);
                            PdfPCell thireIndexscore = this.setPadPCellStyle(thireElement.getString("score"),
                                    contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, thireRowSpan, 1);
                            otherTable.addCell(thireIndexscore);
                            for (Object fourIdx : fourIndex) { // 四级指标
                                JSONObject jsonFourIndex = (JSONObject) fourIdx;
                                String fourParentIndex = jsonFourIndex.getString("parentIndex");
                                if (fourParentIndex.equalsIgnoreCase(thireElement.getString("name"))) {
                                    JSONArray fourContents = jsonFourIndex.getJSONArray("contents");
                                    for (Object fourContent : fourContents) {
                                        JSONObject fourElement = (JSONObject) fourContent;
                                        PdfPCell fourIndexName = this.setPadPCellStyle(fourElement.getString("name"),
                                                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 0);
                                        otherTable.addCell(fourIndexName);
                                        PdfPCell fourIndexscore = this.setPadPCellStyle(fourElement.getString("score"),
                                                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 0);
                                        otherTable.addCell(fourIndexscore);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return otherTable;
    }


    /**
     * @author: getao
     * @Date: 2024/9/29 17:53
     * @Description: 获取模型总体评估表
     */
    public PdfPTable getOverallAssessmentTable(IndexResultInfo resultInfo, Font tableHeaderFont, Font contentFont, int colNumber) {
        PdfPTable firstTable = new PdfPTable(colNumber);
        firstTable.setWidthPercentage(100.0F);
        firstTable.setHorizontalAlignment(Element.ALIGN_MIDDLE);
        //第一列是列表名
        firstTable.setHeaderRows(1);
        firstTable.getDefaultCell().setHorizontalAlignment(1);
        // 表头、表内容
        PdfPCell firstTableTitle = this.setPadPCellStyle("通用一级指标评估分析表",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 5);
        firstTable.addCell(firstTableTitle);
        firstTable.addCell(new Paragraph("指标名称", tableHeaderFont));
        firstTable.addCell(new Paragraph("可用性", contentFont));
        firstTable.addCell(new Paragraph("数据实时性", contentFont));
        firstTable.addCell(new Paragraph("接口符合性", contentFont));
        firstTable.addCell(new Paragraph("格式规范性", contentFont));
        firstTable.addCell(new Paragraph("得分", tableHeaderFont));

        return firstTable;
    }


    /**
     * @author: getao
     * @Date: 2024/9/29 17:53
     * @Description: 获取模型评估通用指标分析表
     */
    public PdfPTable getModelAssessmentFirstTable(JSONArray firstIndex, Font tableHeaderFont, Font contentFont, int colNumber) {
        PdfPTable firstTable = new PdfPTable(colNumber);
        firstTable.setWidthPercentage(100.0F);
        firstTable.setHorizontalAlignment(Element.ALIGN_MIDDLE);
        //第一列是列表名
        firstTable.setHeaderRows(1);
        firstTable.getDefaultCell().setHorizontalAlignment(1);
        // 表头、表内容
        PdfPCell firstTableTitle = this.setPadPCellStyle("通用一级指标评估分析表",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 5);
        firstTable.addCell(firstTableTitle);
        firstTable.addCell(new Paragraph("指标名称", tableHeaderFont));
        firstTable.addCell(new Paragraph("可用性", contentFont));
        firstTable.addCell(new Paragraph("数据实时性", contentFont));
        firstTable.addCell(new Paragraph("接口符合性", contentFont));
        firstTable.addCell(new Paragraph("格式规范性", contentFont));
        firstTable.addCell(new Paragraph("得分", tableHeaderFont));
        firstTable.addCell(new Paragraph(firstIndex.getJSONObject(0).getString("value"), contentFont));
        firstTable.addCell(new Paragraph(firstIndex.getJSONObject(1).getString("value"), contentFont));
        firstTable.addCell(new Paragraph(firstIndex.getJSONObject(2).getString("value"), contentFont));
        firstTable.addCell(new Paragraph(firstIndex.getJSONObject(3).getString("value"), contentFont));

        return firstTable;
    }


    /**
     * @author: getao
     * @Date: 2024/9/29 17:53
     * @Description: 获取模型评估参考表
     */
    public PdfPTable getModelAssessmentRefTable(Font tableHeaderFont, Font contentFont, int colNumber) {
        PdfPTable referenceTable = new PdfPTable(colNumber);
        referenceTable.setWidthPercentage(100.0F);
        // 表头
        PdfPCell refTableTitle = this.setPadPCellStyle("模型评估指标框架组成以及评估参考范围表",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 9);
        referenceTable.addCell(refTableTitle);
        // 内容
        PdfPCell scoreHeader = this.setPadPCellStyle("总体评分",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(scoreHeader);
        PdfPCell scoreHeaderContent = this.setPadPCellStyle("取值0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 8);
        referenceTable.addCell(scoreHeaderContent);

        PdfPCell contibutionHeader = this.setPadPCellStyle("体系贡献率",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(contibutionHeader);
        PdfPCell contibutionContent = this.setPadPCellStyle("取值0%~100%",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 8);
        referenceTable.addCell(contibutionContent);

        PdfPCell firstHeader = this.setPadPCellStyle("一级指标",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(firstHeader);
        PdfPCell firstUsabilityContent = this.setPadPCellStyle("可用性",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstUsabilityContent);
        PdfPCell firstRealTimeContent = this.setPadPCellStyle("数据实时性",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstRealTimeContent);
        PdfPCell firstConformityContent = this.setPadPCellStyle("接口符合性",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstConformityContent);
        PdfPCell firstNormativeContent = this.setPadPCellStyle("格式规范性",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstNormativeContent);
        PdfPCell firstResultHeader = this.setPadPCellStyle("取值",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(firstResultHeader);
        PdfPCell firstResultUsabilityContent = this.setPadPCellStyle("可用 / 不可用",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstResultUsabilityContent);
        PdfPCell firstResultRealTimeContent = this.setPadPCellStyle("实时 / 不实时",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstResultRealTimeContent);
        PdfPCell firstResultConformityContent = this.setPadPCellStyle("符合 / 不符合",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstResultConformityContent);
        PdfPCell firstResultNormativeContent = this.setPadPCellStyle("规范 / 不规范",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(firstResultNormativeContent);

        PdfPCell secondHeader = this.setPadPCellStyle("二级指标",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(secondHeader);
        PdfPCell secondIndex1 = this.setPadPCellStyle("指标名称",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 8);
        referenceTable.addCell(secondIndex1);
        PdfPCell secondResltHeader = this.setPadPCellStyle("取值",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(secondResltHeader);
        PdfPCell secondIndex1Content = this.setPadPCellStyle("0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 8);
        referenceTable.addCell(secondIndex1Content);

        PdfPCell thireHeader = this.setPadPCellStyle("三级指标",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(thireHeader);
        PdfPCell thireIndex1 = this.setPadPCellStyle("指标名称1",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 4);
        referenceTable.addCell(thireIndex1);
        PdfPCell thireIndex2 = this.setPadPCellStyle("指标名称2",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 4);
        referenceTable.addCell(thireIndex2);
        PdfPCell thireResltHeader = this.setPadPCellStyle("取值",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(thireResltHeader);
        PdfPCell thireIndex1Content = this.setPadPCellStyle("0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 4);
        referenceTable.addCell(thireIndex1Content);
        PdfPCell thireIndex2Content = this.setPadPCellStyle("0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 4);
        referenceTable.addCell(thireIndex2Content);

        PdfPCell fourHeader = this.setPadPCellStyle("四级指标",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(fourHeader);
        PdfPCell fourIndex1 = this.setPadPCellStyle("指标名称1",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex1);
        PdfPCell fourIndex2 = this.setPadPCellStyle("指标名称2",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex2);
        PdfPCell fourIndex3 = this.setPadPCellStyle("指标名称3",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex3);
        PdfPCell fourIndex4 = this.setPadPCellStyle("指标名称4",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex4);
        PdfPCell fourResltHeader = this.setPadPCellStyle("取值",
                tableHeaderFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 1);
        referenceTable.addCell(fourResltHeader);
        PdfPCell fourIndex1Content = this.setPadPCellStyle("0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex1Content);
        PdfPCell fourIndex2Content = this.setPadPCellStyle("0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex2Content);
        PdfPCell fourIndex3Content = this.setPadPCellStyle("0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex3Content);
        PdfPCell fourIndex4Content = this.setPadPCellStyle("0~100",
                contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, 1, 2);
        referenceTable.addCell(fourIndex4Content);

        return referenceTable;
    }


    /**
     * 外部数据引接更改任务状态信息
     */
    @Override
    public void changeTaskStatus(Integer taskId, String status) {
        SimulateTaskInfo taskInfo = new SimulateTaskInfo();
        taskInfo.setStatus(status);
        taskInfo.setId(taskId);
        baseMapper.updateById(taskInfo);
    }

    @Override
    public boolean queryIsWait(Integer taskId) {
        SimulateTaskInfo taskInfo = baseMapper.selectById(taskId);
        return "WAIT".equalsIgnoreCase(taskInfo.getStatus());
    }

    @Override
    public void checkStatusAndSetFail() {
        List<String> exclude = Arrays.asList("WAIT", "FINISH", "ERROR", "AMT_FINISH");
        LambdaQueryWrapper<SimulateTaskInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.notIn(SimulateTaskInfo::getStatus, exclude);
        SimulateTaskInfo info = new SimulateTaskInfo();
        info.setStatus("FAIL");
        baseMapper.update(info, lambdaQueryWrapper);
    }

    @Override
    public List<String> getModelSignByTaskId(int taskId) {
        SimulateTaskInfo simulateTaskInfo = baseMapper.selectById(taskId);
        if (simulateTaskInfo == null) {
            throw new RuntimeException("当前任务id对应的数据在数据库中不存在, 请检查");
        }
        String[] modelIdsStr = simulateTaskInfo.getModelId().split(",");
        List<String> result = new ArrayList<>();
        for (String modelId : modelIdsStr) {
            ModelInfo modelInfo = modelService.getById(Integer.valueOf(modelId));
            if (modelInfo != null) {
                result.add(modelInfo.getSign());
            }
        }
        if (result.isEmpty()) {
            throw new CommonException("当前任务对应的模型信息已经被删除，数据库中不存在对应的模型信息");
        }
        return result;
    }


    @Override
    public ModelInfo modelConfig(int modelId, int indexSystemId) {
        ModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        IndexSystemInfo indexSystemInfo = this.indexSystemService.getById(indexSystemId);
        JSONObject indexs = this.indexInfoService.getIndexBySignAndBatchNo(modelInfo.getSign(), indexSystemInfo.getBatchNo());
        modelInfo.setIndexInfos(indexs);
        return modelInfo;
    }


    @Async
    @Override
    public JSONArray startAssessment(int taskId) {
        JSONArray assessmentResult = new JSONArray();

        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        String modelIds = taskInfo.getModelId();
        String weight = taskInfo.getModelWeight();
        String indexSystemIds = taskInfo.getIndexSystemId();
        List<Integer> indexSystemList = Arrays.stream(indexSystemIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> weightList = Arrays.stream(weight.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        // 指标评估

        for (Integer modelId : modelIdList) {
            try {// 评估
                ModelTypeService modelTypeService = commonService.getModelTypeService(modelId);
                JSONArray runRs = modelTypeService.startAssessment(taskId, modelId, assessmentResult, indexSystemList, modelIdList, weightList);
                // 更新评估记录
                this.assessmentService.updateAsmInfo(taskId, modelId, "AMT_FINISH");
            } catch (Exception e) {
                log.error("==>> 模型 {} 评估失败 {} ...", modelId, e.getMessage());
                taskInfo.setStatus("FAIL");
                return assessmentResult;
            }
        }

        taskInfo.setStatus("AMT_FINISH");
        taskInfo.setFinishTime(cn.iecas.simulate.assessment.util.DateUtils.currentTimeDate());
        this.updateById(taskInfo);
        // 体系贡献率评估
        //this.contibutionAssessment(assessmentResult);

        return assessmentResult;
    }


    /**
     * 更新评估记录信息
     *
     * @param taskId
     * @param modelId
     * @param status
     */
    private void updateAsmInfo(int taskId, int modelId, String status) {
        QueryWrapper<AssessmentResultInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).eq("model_id", modelId);
        List<AssessmentResultInfo> list = this.resultService.list(wrapper);
        if (list == null || list.size() == 0)
            return;

        try {
            List<String> asmValues = list.stream().map(AssessmentResultInfo::getValue).collect(Collectors.toList());
            JSONObject asmAvg = JSONObject.parseObject(JSONUtils.mergeJsonStrAndCalculate(asmValues));
            UpdateWrapper<ModelAssessmentInfo> update = new UpdateWrapper<>();
            update.eq("task_id", taskId).eq("model_id", modelId)
                    .set("assessment_score", asmAvg.getDouble("score")).set("status", status);
            this.assessmentService.update(update);
        } catch (JsonProcessingException jsonProcessingException) {
            throw new RuntimeException(jsonProcessingException);
        }
    }


    /**
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     * @Description 获取敏感度分析结果
     * @Author getao
     * @Date 8:57 2025/3/21
     * @Param [taskId, modelId]
     */
    @Override
    public List<JSONObject> getDataBorderResult(int taskId) {
        QueryWrapper<AssessmentResultInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        List<AssessmentResultInfo> list = this.resultService.list(wrapper);
        if (list == null || list.size() == 0)
            return new ArrayList<>();

        SimulateTaskInfo taskInfo = this.getById(taskId);
        String modelIds = taskInfo.getModelId();
        String weights = taskInfo.getModelWeight();
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        List<JSONObject> result = new ArrayList<>();
        Map<Integer, List<AssessmentResultInfo>> asmRsByModel = list.stream().collect(Collectors.groupingBy(AssessmentResultInfo::getModelId));
        for (Map.Entry<Integer, List<AssessmentResultInfo>> entry : asmRsByModel.entrySet()) {
            Integer modelId = entry.getKey();
            List<AssessmentResultInfo> asmByModel = entry.getValue();
            ModelInfo modelInfo = this.modelService.getById(modelId);

            Map<Integer, List<AssessmentResultInfo>> asmRsByDataNo = asmByModel.stream().collect(Collectors.groupingBy(AssessmentResultInfo::getDataNo));
            List<JSONObject> asmShowByDataNo = asmRsByDataNo.entrySet().stream().map(e -> {
                JSONObject rs = new JSONObject();
                try {
                    List<String> values = e.getValue().stream().map(AssessmentResultInfo::getValue).collect(Collectors.toList());
                    JSONObject avgValue = JSONObject.parseObject(JSONUtils.mergeJsonStrAndCalculate(values));
                    rs.put("modelName", modelInfo.getModelName());
                    rs.put("fileName", "Group " + "NO " + e.getValue().get(0).getDataNo());
                    rs.put("score", avgValue.get("score"));
                } catch (JsonProcessingException jsonProcessingException) {
                    jsonProcessingException.printStackTrace();
                }
                return rs;
            }).collect(Collectors.toList());
            result.addAll(asmShowByDataNo);
        }

        return result;
    }


    /**
     * @return java.util.List<com.alibaba.fastjson.JSONObject>
     * @Description 获取敏感度分析结果
     * @Author getao
     * @Date 8:56 2025/3/21
     * @Param [taskId]
     */
    @Override
    public List<JSONObject> getSensitivity(int taskId) {
        QueryWrapper<AssessmentResultInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        List<AssessmentResultInfo> list = this.resultService.list(wrapper);
        if (list == null || list.size() == 0)
            return new ArrayList<>();

        SimulateTaskInfo taskInfo = this.getById(taskId);
        String modelIds = taskInfo.getModelId();
        String weights = taskInfo.getModelWeight();
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> weightList = Arrays.stream(weights.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        List<JSONObject> result = new ArrayList<>();
        Map<Integer, List<AssessmentResultInfo>> asmRsByModel = list.stream().collect(Collectors.groupingBy(AssessmentResultInfo::getModelId));
        for (Map.Entry<Integer, List<AssessmentResultInfo>> entry : asmRsByModel.entrySet()) {
            Integer modelId = entry.getKey();
            int indexOf = modelIdList.indexOf(modelId);
            Integer weight = weightList.get(indexOf);
            List<AssessmentResultInfo> asmByModel = entry.getValue();
            ModelInfo modelInfo = this.modelService.getById(modelId);

            List<Integer> runDataIds = asmByModel.stream().map(AssessmentResultInfo::getRunDataId).collect(Collectors.toList());
            Collection<ModelRunlDataInfo> runlDataInfos = this.runDataService.listByIds(runDataIds);
            Map<String, List<ModelRunlDataInfo>> runDataByInPath = runlDataInfos.stream().collect(Collectors.groupingBy(ModelRunlDataInfo::getInputPath));
            List<JSONObject> element = runDataByInPath.entrySet().stream().map(runData -> {
                String inputPath = runData.getKey();
                List<Integer> rundataIds = runData.getValue().stream().map(ModelRunlDataInfo::getId).collect(Collectors.toList());
                List<AssessmentResultInfo> asmsByInpath = asmByModel.stream().filter(e -> rundataIds.contains(e.getRunDataId())).collect(Collectors.toList());
                JSONObject rs = new JSONObject();
                double[] data = asmsByInpath.stream().mapToDouble(e -> {
                    JSONObject asm = JSONObject.parseObject(e.getValue());
                    return asm.getDouble("score") * weight;
                }).toArray();
                double populaTionVariance = MathUtils.populaTionVariance(data);
                double sampleVariance = MathUtils.sampleVariance(data);
                rs.put("modelName", modelInfo.getModelName());
                rs.put("fileName", inputPath);
                rs.put("populaTionVariance", populaTionVariance);
                rs.put("sampleVariance", sampleVariance);
                return rs;
            }).collect(Collectors.toList());
            result.addAll(element);
        }

        return result;
    }
}
