package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.ModelDao;
import cn.iecas.simulate.assessment.dao.ModelShareDao;
import cn.iecas.simulate.assessment.dao.SimulateTaskDao;
import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.ModelAssessmentInfo;
import cn.iecas.simulate.assessment.entity.domain.ModelShareInfo;
import cn.iecas.simulate.assessment.entity.domain.SimulateTaskInfo;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.entity.dto.ModelShareDTO;
import cn.iecas.simulate.assessment.service.ModelAssessmentService;
import cn.iecas.simulate.assessment.service.ModelShareService;
import cn.iecas.simulate.assessment.util.UserUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        int successCount = 0, failCount = 0;
        for (Integer id : idList) {
            if (saveOne(id, userId, message)) {
                successCount++;
            } else
                failCount++;
        }
        resultMap.put("successCount", successCount);
        resultMap.put("failCount", failCount);
        resultMap.put("failMessage", message);
        return resultMap;
    }


    private Boolean saveOne(Integer shareId, Integer userId, List<String> message) {
        ModelAssessmentInfo shareInfo = modelAssessmentService.getById(shareId);

        Integer isExist = baseMapper.selectCount(new LambdaQueryWrapper<ModelShareInfo>()
                .eq(ModelShareInfo::getTaskId, shareInfo.getTaskId())
                .eq(ModelShareInfo::getModelId, String.valueOf(shareInfo.getModelId()))
                .eq(ModelShareInfo::getAssessmentId, shareId)
                .eq(ModelShareInfo::getDelete, false));
        if (isExist == 1){
            message.add("id: " + shareId + " -> 当前评估已被共享过，无需再次共享");
            return false;     // 防止一个任务被共享多次
        }
        ModelShareInfo modelShareInfo = new ModelShareInfo();
        modelShareInfo.setTaskId(shareInfo.getTaskId());
        SimulateTaskInfo simulateTaskInfo = simulateTaskDao.selectById(shareInfo.getTaskId());
        modelShareInfo.setTaskName(simulateTaskInfo.getTaskName());
        modelShareInfo.setModelName(shareInfo.getModelName());
        modelShareInfo.setModelId(String.valueOf(shareInfo.getModelId()));
        modelShareInfo.setTaskType(simulateTaskInfo.getTaskType());
        modelShareInfo.setUserId(userId);
        modelShareInfo.setShareTime(new Date());
        modelShareInfo.setUnit(simulateTaskInfo.getUnit());
        modelShareInfo.setUserLevel(simulateTaskInfo.getUserLevel());
        modelShareInfo.setAssessmentId(shareId);
        if (1 == baseMapper.insert(modelShareInfo)) {
            return true;
        } else {
            message.add("id: " + shareId + " -> 任务插入数据库失败");
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
    public Map<String, Object> getModelStatistics() {
        LambdaQueryWrapper<ModelShareInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelShareInfo::getDelete, false);
        List<ModelShareInfo> modelShares = ModelShareDao.selectList(queryWrapper);
        Set<String> uniqueModelNames = new HashSet<>();
        for (ModelShareInfo modelShare : modelShares) {
            String modelName = modelShare.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                uniqueModelNames.add(modelName);
            }
        }
        int totalCount = uniqueModelNames.size();
        Map<String, Integer> countMap = new HashMap<>();
        for (ModelShareInfo modelShare : modelShares) {
            String modelName = modelShare.getModelName();
            if (modelName != null && !modelName.isEmpty()) {
                countMap.put(modelName, countMap.getOrDefault(modelName, 0) + 1);
            }
        }
        // 计算百分比
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            String modelName = entry.getKey();
            int count = entry.getValue();
            double percentage = totalCount > 0 ? (double) count / totalCount * 100 : 0; // 避免除以0
            Map<String, Object> modelDetails = new HashMap<>();
            modelDetails.put("count", count);
            modelDetails.put("percentage", percentage);
            result.put(modelName, modelDetails);
        }
        return result;
    }


    @Override
    public List<TbModelInfo> getModelAssessmentType() {
        QueryWrapper<ModelShareInfo> model_id = new QueryWrapper<ModelShareInfo>().select("model_id").groupBy("model_id");
        List<ModelShareInfo> modelShareInfos = this.ModelShareDao.selectList(model_id);
        if (modelShareInfos.size() == 0) {
            return new ArrayList<>();
        }
        List<String> modelIds = modelShareInfos.stream().map(ModelShareInfo::getModelId).collect(Collectors.toList());
        Set<Integer> modelIdSet = modelIds.stream().map(e -> e.split(",")).flatMap(Arrays::stream).map(Integer::parseInt).collect(Collectors.toSet());
        QueryWrapper<TbModelInfo> wrapper = new QueryWrapper<TbModelInfo>().in("id", modelIdSet).select("service_type", "COUNT(*) AS assessmentCount").groupBy("service_type");
        List<TbModelInfo> result = this.modelDao.selectList(wrapper);
        return result;
    }
}



