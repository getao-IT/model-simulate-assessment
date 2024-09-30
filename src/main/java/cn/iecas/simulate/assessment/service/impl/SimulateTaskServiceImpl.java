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
import cn.iecas.simulate.assessment.util.OfficeUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
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

    @Autowired
    private HttpServletResponse response;

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
            TbModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
            // 获取任务对应该模型的仿真数据
            AssessmentResultInfo resultInfo = new AssessmentResultInfo();
            resultInfo.setModelId(modelId);
            resultInfo.setTaskId(taskId);
            List<SimulateDataInfo> simulateDatas = this.dataService.getSimulateDataByModel(taskId, modelId);
            modelAssessment.put("modelId", modelId);
            modelAssessment.put("name", modelInfo.getModelName()+"评估结果");
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
            if (modelInfo.getSign().contains("FHGXFX")) {
                resultInfo = analysisService.getFHGXFXAssessmentInfo(assessmentDatas, indexSystemId, resultInfo);
            }
            if (modelInfo.getSign().contains("ZDRWLLFX")) {
                resultInfo = analysisService.getFHGXFXAssessmentInfo(assessmentDatas, indexSystemId, resultInfo);
            }
            modelAssessment.put("value", resultInfo);
            assessmentResult.add(modelAssessment);
            //assessmentResult.put(taskId + "-" + modelId, resultInfo);
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
    private JSONArray contibutionAssessment(JSONArray assessmentResultArr) {
        double totalScore = 0;

        for (Object o : assessmentResultArr) {
            JSONObject assessmentResult = (JSONObject) o;
            JSONObject assessmentInfo = assessmentResult.getJSONObject("value");
            Double score = assessmentInfo.getDouble("score") == null ? 0 : assessmentInfo.getDouble("score");
            int weight = assessmentInfo.getInteger("weight");
            totalScore += score * (weight / 100.0);
        }

        for (Object o : assessmentResultArr) {
            JSONObject assessmentResult = (JSONObject) o;
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
     *  @author: getao
     *  @Date: 2024/9/26 16:08
     *  @Description: 导出模型评估报告
     */
    @Override
    public void exportAssessmentReport(int taskId, int modelId, double contibution) {
        JSONObject assessmentResult = new JSONObject();

        SimulateTaskInfo taskInfo = this.taskDao.selectById(taskId);
        String modelIds = taskInfo.getModelId();
        String weight = taskInfo.getModelWeight();
        String indexSystemIds = taskInfo.getIndexSystemId();
        List<Integer> indexSystemList = Arrays.stream(indexSystemIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> modelIdList = Arrays.stream(modelIds.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Integer> weightList = Arrays.stream(weight.split(",")).map(Integer::parseInt).collect(Collectors.toList());

        //// 指标评估
        TbModelInfo modelInfo = this.modelService.getModelInfoById(modelId);
        // 获取任务对应该模型的仿真数据
        AssessmentResultInfo resultInfo = new AssessmentResultInfo();
        resultInfo.setTaskId(taskId);
        resultInfo.setModelId(modelId);
        List<SimulateDataInfo> simulateDatas = this.dataService.getSimulateDataByModel(taskId, modelId);
        if (simulateDatas.size() == 0) {
            assessmentResult.put(taskId + "-" + modelId, resultInfo);
            return;
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

        try {
            // 第一步，实例化一个document对象
            Document document = new Document();
            // 第二步，设置要到出的路径
            //FileOutputStream out = new FileOutputStream("C:\\Users\\Administrator\\Downloads\\exportPdf\\" + modelInfo.getModelName() + "_模型评估报告" + "_" + System.currentTimeMillis() + ".pdf");
            //如果是浏览器通过request请求需要在浏览器中输出则使用下面方式
            OutputStream out = response.getOutputStream();
            // 第三步,设置字符
            BaseFont yhFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            BaseFont stFont = BaseFont.createFont("C:/windows/fonts/simsun.ttc,1", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            //BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", false);
            Font titleFont = new Font(yhFont, 16.0F, Font.BOLD);
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
            Paragraph modelName = new Paragraph(new Chunk("模型名称："+modelInfo.getModelName(), tableDescriptFont));
            document.add(modelName);
            Paragraph assessmentTime = new Paragraph(new Chunk("评估时间："+DateUtils.nowDate(), tableDescriptFont));
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
            String assessmentConclusion = this.getAssessmentConclusion(resultInfo);
            Paragraph modelAssessmentResult = new Paragraph(new Chunk(assessmentConclusion, contentFont));
            modelAssessmentResult.setFirstLineIndent(10);
            document.add(modelAssessmentResultTitle);
            document.add(new Paragraph("\n"));
            document.add(modelAssessmentResult);
            document.add(new Paragraph("\n"));

            // 模型评估参考值表
            Paragraph modelAssessmentRefTitle = new Paragraph(new Chunk("模型评估参考表", tableHeaderFont));
            PdfPTable assessmentRefTable = this.getModelAssessmentRefTable(tableHeaderFont,contentFont, 9);
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
     *  @author: getao
     *  @Date: 2024/9/29 12:47
     *  @Description: 获取某级别某指标名称包含的子指标行数
     */
    private int getRowSpanNumber(AssessmentResultInfo resultInfo, int indexLevel, String indexName) {
        int rowSpan = 0;
        JSONArray thireIndex = resultInfo.getThreeIndex();
        JSONArray fourIndex = resultInfo.getFourIndex();

        if (indexLevel == 3) {
            for (Object fourIdx : fourIndex) { // 四级指标
                JSONObject jsonFourIndex = (JSONObject)fourIdx;
                String fourParentIndex = jsonFourIndex.getString("parentIndex");
                if (fourParentIndex.equalsIgnoreCase(indexName)) {
                    JSONArray fourContents = jsonFourIndex.getJSONArray("contents");
                    rowSpan += fourContents.size();
                }
            }
        }

        if (indexLevel == 2) {
            for (Object thireIdx : thireIndex) { // 三级指标
                JSONObject jsonThireIndex = (JSONObject)thireIdx;
                String thireParentIndex = jsonThireIndex.getString("parentIndex");
                if (thireParentIndex.equalsIgnoreCase(indexName)) {
                    JSONArray thireContents = jsonThireIndex.getJSONArray("contents");
                    for (Object thireContent : thireContents) {
                        JSONObject thireElement = (JSONObject)thireContent;
                        String thireIndexName = thireElement.getString("name");
                        for (Object fourIdx : fourIndex) { // 四级指标
                            JSONObject jsonFourIndex = (JSONObject)fourIdx;
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
     *  @author: getao
     *  @Date: 2024/9/29 13:02
     *  @Description: 设置单元格格式
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
     *  @author: getao
     *  @Date: 2024/9/29 13:02
     *  @Description: 设置单元格格式
     */
    private String getAssessmentConclusion(AssessmentResultInfo assessmentInfo) {
        String sceneName = "--";
        String modelName = "--";
        Double score = assessmentInfo.getScore();
        String scoreAssess = "";
        int weight = assessmentInfo.getWeight();
        Double contibution = assessmentInfo.getContibution();
        String contibutionAssess = "";

        SimulateTaskInfo simulateTaskInfo = this.getById(assessmentInfo.getTaskId());
        sceneName = simulateTaskInfo.getSceneName();
        TbModelInfo modelInfo = this.modelService.getModelInfoById(assessmentInfo.getModelId());
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
     *  @author: getao
     *  @Date: 2024/9/30 10:18
     *  @Description: 获取其他指标评估分析表
     */
    public PdfPTable getModelAssessmentOtherTable(AssessmentResultInfo resultInfo, String modelName, Font tableHeaderFont,
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
            JSONObject jsonSecondIndex = (JSONObject)secondIdx;
            JSONArray secondContents = jsonSecondIndex.getJSONArray("contents");
            for (Object secondContent : secondContents) {
                JSONObject secondElement = (JSONObject)secondContent;
                int secondRowSpan = getRowSpanNumber(resultInfo, 2, secondElement.getString("name"));
                PdfPCell secondIndexName = this.setPadPCellStyle(secondElement.getString("name"),
                        contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, secondRowSpan, 1);
                otherTable.addCell(secondIndexName);
                PdfPCell secondIndexscore = this.setPadPCellStyle(secondElement.getString("score"),
                        contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, secondRowSpan, 1);
                otherTable.addCell(secondIndexscore);
                for (Object thireIdx : thireIndex) { // 三级指标
                    JSONObject jsonThireIndex = (JSONObject)thireIdx;
                    String thireParentIndex = jsonThireIndex.getString("parentIndex");
                    if (thireParentIndex.equalsIgnoreCase(secondElement.getString("name"))) {
                        JSONArray thireContents = jsonThireIndex.getJSONArray("contents");
                        for (Object thireContent : thireContents) {
                            JSONObject thireElement = (JSONObject)thireContent;
                            int thireRowSpan = getRowSpanNumber(resultInfo, 3, thireElement.getString("name"));
                            PdfPCell thireIndexName = this.setPadPCellStyle(thireElement.getString("name"),
                                    contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, thireRowSpan, 1);
                            otherTable.addCell(thireIndexName);
                            PdfPCell thireIndexscore = this.setPadPCellStyle(thireElement.getString("score"),
                                    contentFont, Element.ALIGN_CENTER, Element.ALIGN_MIDDLE, thireRowSpan, 1);
                            otherTable.addCell(thireIndexscore);
                            for (Object fourIdx : fourIndex) { // 四级指标
                                JSONObject jsonFourIndex = (JSONObject)fourIdx;
                                String fourParentIndex = jsonFourIndex.getString("parentIndex");
                                if (fourParentIndex.equalsIgnoreCase(thireElement.getString("name"))) {
                                    JSONArray fourContents = jsonFourIndex.getJSONArray("contents");
                                    for (Object fourContent : fourContents) {
                                        JSONObject fourElement = (JSONObject)fourContent;
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
     *  @author: getao
     *  @Date: 2024/9/29 17:53
     *  @Description: 获取模型总体评估表
     */
    public PdfPTable getOverallAssessmentTable(AssessmentResultInfo resultInfo, Font tableHeaderFont, Font contentFont, int colNumber) {
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
     *  @author: getao
     *  @Date: 2024/9/29 17:53
     *  @Description: 获取模型评估通用指标分析表
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
     *  @author: getao
     *  @Date: 2024/9/29 17:53
     *  @Description: 获取模型评估参考表
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
}
