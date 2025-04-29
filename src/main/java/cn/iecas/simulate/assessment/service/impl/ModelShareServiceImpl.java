package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.ModelDao;
import cn.iecas.simulate.assessment.dao.ModelShareDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentInfo;
import cn.iecas.simulate.assessment.entity.domain.ModelShareInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.domain.ModelInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelShareDTO;
import cn.iecas.simulate.assessment.service.ModelAssessmentService;
import cn.iecas.simulate.assessment.service.ModelShareService;
import cn.iecas.simulate.assessment.util.UserUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;



/**
 * @Time: 2024/10/29 11:17
 * @Author: guoxun
 * @File: ModelShareServiceImpl
 * @Description:
 */
@Service
public class ModelShareServiceImpl extends ServiceImpl<ModelShareDao, ModelShareInfo> implements ModelShareService {

    @Autowired
    private ModelShareDao ModelShareDao;

    @Autowired
    private SimulateTaskDao simulateTaskDao;

    @Autowired
    private ModelAssessmentService modelAssessmentService;

    @Autowired
    private ModelDao modelDao;

    @Autowired
    private UserUtils userUtils;


    @Override
    public Map<String, Object> share(List<Integer> idList) {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> message = new ArrayList<>();
        int userId = userUtils.getUserIdByToken();
        JSONObject userInfo = userUtils.getUserJsonInfoByToken();
        int successCount = 0, failCount = 0;
        for (Integer id : idList) {
            if (saveOne(id, userId, message, resultMap, userInfo.getString("name"))) {
                successCount++;
            } else
                failCount++;
        }
        resultMap.put("successCount", successCount);
        resultMap.put("failCount", failCount);
        resultMap.put("failMessage", message);
        return resultMap;
    }


    private Boolean saveOne(Integer shareId, Integer userId, List<String> message, Map<String, Object> resultMap,
                            String shareUserName) {
        ModelAssessmentInfo shareInfo = modelAssessmentService.getById(shareId);

        Integer isExist = baseMapper.selectCount(new LambdaQueryWrapper<ModelShareInfo>()
                .eq(ModelShareInfo::getTaskId, shareInfo.getTaskId())
                .eq(ModelShareInfo::getModelId, String.valueOf(shareInfo.getModelId()))
                .eq(ModelShareInfo::getAssessmentId, shareId)
                .eq(ModelShareInfo::getDelete, false));
        if (isExist == 1){
            message.add("id: " + shareId + " -> 当前评估已被共享过，无需再次共享");
            resultMap.put("message", "当前模型已经共享, 无需再次共享");
            resultMap.put("status", "false");
            return false;     // 防止一个任务被共享多次
        }
        ModelShareInfo modelShareInfo = new ModelShareInfo();
        modelShareInfo.setTaskId(shareInfo.getTaskId());
        SimulateTaskInfo simulateTaskInfo = simulateTaskDao.selectById(shareInfo.getTaskId());
        if (simulateTaskInfo == null){
            resultMap.put("message", "当前评估信息所对应的任务已被删除，无法共享!");
            resultMap.put("status", "false");
            return message.add("id: " + shareId + "数据库数据不一致，不存在该评估所对应的task任务!");
        }
        modelShareInfo.setTaskName(simulateTaskInfo.getTaskName());
        modelShareInfo.setModelName(shareInfo.getModelName());
        modelShareInfo.setAssessmentScore(shareInfo.getAssessmentScore());
        modelShareInfo.setModelId(String.valueOf(shareInfo.getModelId()));
        modelShareInfo.setTaskType(simulateTaskInfo.getTaskType());
        modelShareInfo.setUserId(userId);
        modelShareInfo.setShareTime(new Date());
        modelShareInfo.setUnit(simulateTaskInfo.getUnit());
        modelShareInfo.setUserLevel(simulateTaskInfo.getUserLevel());
        modelShareInfo.setAssessmentId(shareId);
        modelShareInfo.setShareUser(shareUserName);
        if (1 == baseMapper.insert(modelShareInfo)) {
            resultMap.put("status", "true");
            return true;
        } else {
            resultMap.put("message", "插入失败");
            message.add("id: " + shareId + " -> 任务插入数据库失败");
            resultMap.put("status", "false");
            return false;
        }
    }


    @Override
    public void delete(List<Integer> idList) {
        LambdaUpdateChainWrapper<ModelShareInfo> updateChainWrapper = new LambdaUpdateChainWrapper<>(baseMapper);
        updateChainWrapper.in(ModelShareInfo::getId, idList).set(ModelShareInfo::getDelete, true)
                .update();
    }


    @Override
    public PageResult<ModelShareInfo> getShareModelInfo(ModelShareDTO dto) {
        QueryWrapper<ModelShareInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("delete", false)
                .like(dto.getModelName() != null, "model_name", dto.getModelName())
                .like(dto.getUnit() != null, "unit", dto.getUnit())
                .like(dto.getFuzzy() != null, "CONCAT(user_level, task_name, model_name" +
                        ",unit, task_type)", dto.getFuzzy())
                .ge(dto.getGeTime() != null, "share_time", dto.getGeTime())
                .le(dto.getLeTime() != null, "share_time", dto.getLeTime())
                .orderByDesc(dto.getOrderCol() == null, "share_time")
                .orderByDesc(dto.getOrderCol() != null
                        && dto.getOrderWay().equalsIgnoreCase("desc"), dto.getOrderCol())
                .orderByAsc(dto.getOrderCol() != null
                        && dto.getOrderWay().equalsIgnoreCase("asc"), dto.getOrderCol());
        IPage<ModelShareInfo> pageResult = baseMapper.selectPage(
                new Page<>(dto.getPageNo(), dto.getPageSize()), wrapper);
        return new PageResult<>(pageResult.getCurrent(), pageResult.getTotal(), pageResult.getRecords());
    }


    @Override
    public Integer getAssessmentTotal() {
        return ModelShareDao.getAssessmentTotal();
    }


    @Override
    public Integer getModelTotal() {
        return ModelShareDao.getModelTotal();
    }


    @Override
    public List<Map<String, Object>> getModelPercent() {
        LambdaQueryWrapper<ModelShareInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelShareInfo::getDelete, false);
        List<ModelShareInfo> modelShares = ModelShareDao.selectList(queryWrapper);
        int totalCount = modelShares.size();
        Map<String, Integer> countMap = new HashMap<>();
        for (ModelShareInfo modelShare : modelShares) {
            String modelName = modelShare.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                countMap.put(modelName, countMap.getOrDefault(modelName, 0) + 1);
            }
        }
        return countMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> modelData = new HashMap<>();
                    modelData.put("name", entry.getKey());  // 模型名称
                    modelData.put("count", entry.getValue()); // 模型计数
                    double percent = totalCount > 0 ? (double) entry.getValue() / totalCount : 0;
                    modelData.put("percent", percent); // 百分比
                    return modelData;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ModelShareInfo judgementById(Integer id, Double judgementScore, Integer judgementStatus) {
        JSONObject userInfo = userUtils.getUserJsonInfoByToken();
        LambdaUpdateChainWrapper<ModelShareInfo> updateChainWrapper = new LambdaUpdateChainWrapper<>(baseMapper);
        updateChainWrapper.eq(ModelShareInfo::getId, id)
                        .set(judgementScore != null, ModelShareInfo::getJudgementScore, judgementScore)
                        .set(judgementStatus != null, ModelShareInfo::getJudgementStatus, judgementStatus)
                        .set(ModelShareInfo::getJudgementTime, new Date())
                        .set(ModelShareInfo::getJudgementUser, userInfo.get("name"))
                        .set(ModelShareInfo::getJudgementUserId, userInfo.get("id"));
        updateChainWrapper.update();

        ModelShareInfo shareInfo = this.ModelShareDao.selectById(id);
        UpdateWrapper<ModelAssessmentInfo> wrapper = new UpdateWrapper<>();
        wrapper.eq("task_id", shareInfo.getTaskId()).eq("model_id", Integer.parseInt(shareInfo.getModelId()))
                .set("check_score", judgementScore);
        this.modelAssessmentService.update(wrapper);

        return baseMapper.selectById(id);
    }


    @Override
    public PageResult<ModelShareInfo> getJudgementShareModelInfo(ModelShareDTO dto) {
        QueryWrapper<ModelShareInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("delete", false)
                .ne("judgement_status", 0)
                .like(dto.getModelName() != null, "model_name", dto.getModelName())
                .like(dto.getUnit() != null, "unit", dto.getUnit())
                .like(dto.getFuzzy() != null, "CONCAT(user_level, task_name, model_name" +
                        ",unit, task_type)", dto.getFuzzy())
                .ge(dto.getGeTime() != null, "share_time", dto.getGeTime())
                .le(dto.getLeTime() != null, "share_time", dto.getLeTime())
                .orderByDesc(dto.getOrderCol() == null, "share_time")
                .orderByDesc(dto.getOrderCol() != null
                        && dto.getOrderWay().equalsIgnoreCase("desc"), dto.getOrderCol())
                .orderByAsc(dto.getOrderCol() != null
                        && dto.getOrderWay().equalsIgnoreCase("asc"), dto.getOrderCol());
        IPage<ModelShareInfo> pageResult = baseMapper.selectPage(
                new Page<>(dto.getPageNo(), dto.getPageSize()), wrapper);
        return new PageResult<>(pageResult.getCurrent(), pageResult.getTotal(), pageResult.getRecords());
    }


    @Override
    public List<ModelInfo> getModelAssessmentType() {
        QueryWrapper<ModelShareInfo> model_id = new QueryWrapper<ModelShareInfo>().select("model_id").groupBy("model_id");
        List<ModelShareInfo> modelShareInfos = this.ModelShareDao.selectList(model_id);
        if (modelShareInfos.size() == 0) {
            return new ArrayList<>();
        }
        List<String> modelIds = modelShareInfos.stream().map(ModelShareInfo::getModelId).collect(Collectors.toList());
        Set<Integer> modelIdSet = modelIds.stream().map(e -> e.split(",")).flatMap(Arrays::stream).map(Integer::parseInt).collect(Collectors.toSet());
        QueryWrapper<ModelInfo> wrapper = new QueryWrapper<ModelInfo>().in("id", modelIdSet).select("service_type", "COUNT(*) AS assessmentCount").groupBy("service_type");
        List<ModelInfo> result = this.modelDao.selectList(wrapper);
        return result;
    }


    @Override
    public List<Map<String, Object>> getServiceTypeByType() {
        List<ModelShareInfo> modelShares = ModelShareDao.selectList(null);
        Map<String, Long> groupedCounts = modelShares.stream()
                .collect(Collectors.groupingBy(ModelShareInfo::getModelName, Collectors.counting()));
        return groupedCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("Name", entry.getKey());
                    result.put("Value", entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }
}



