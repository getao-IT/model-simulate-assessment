package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.common.exception.CommonException;
import cn.iecas.simulate.assessment.dao.ModelDao;
import cn.iecas.simulate.assessment.dao.SysetemDao;
import cn.iecas.simulate.assessment.entity.domain.IndexSystemInfo;
import cn.iecas.simulate.assessment.entity.domain.SystemInfo;
import cn.iecas.simulate.assessment.entity.domain.ModelInfo;
import cn.iecas.simulate.assessment.entity.emun.ModelType;
import cn.iecas.simulate.assessment.service.ExternalDataAccessService;
import cn.iecas.simulate.assessment.service.IndexSystemService;
import cn.iecas.simulate.assessment.service.ModelService;
import cn.iecas.simulate.assessment.service.assessment.ModelTypeService;
import cn.iecas.simulate.assessment.util.DateUtils;
import cn.iecas.simulate.assessment.util.UserUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import io.swagger.models.Model;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;



/**
 * @auther cyl
 * @date 2024/8/19
 * @description 模型信息服务接口实现类
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelDao, ModelInfo> implements ModelService {

    @Autowired
    private ModelDao modelDao;

    @Autowired
    private SysetemDao systemDao;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private IndexSystemService indexSystemService;


    //查询模型信息
    @Override
    public IPage<ModelInfo> getModelInfo(ModelInfo modelInfo) {
        QueryWrapper<ModelInfo> queryWrapper = new QueryWrapper<>();
        //获取当前用户请求的信息
        JSONObject userJsonInfoByToken = userUtils.getUserJsonInfoByToken();
        // 检查用户是否为管理员或超级管理员
        boolean isAdmin = userJsonInfoByToken.getBoolean("is_admin");
        boolean isSuperAdmin = userJsonInfoByToken.getBoolean("is_super_admin");
        Integer currentUserUid = userJsonInfoByToken.getInteger("id");

        // 筛选systemIds
        List<Integer> systemIds = systemDao.findSystemStatus();
        Page<ModelInfo> page = new Page<>(modelInfo.getPageNo(), modelInfo.getPageSize());

        // 管理员或超级管理员可以查看所有数据
        if (!(isAdmin || isSuperAdmin)) {
            // 其他用户需要根据创建者或 is_model_visible字段进行过滤
            List<SystemInfo> systemInfoList = systemDao.selectList(new LambdaQueryWrapper<SystemInfo>()
                    .eq(SystemInfo::getUid, currentUserUid));
            List<Integer> creatorSystemIds = new ArrayList<>();
            for (SystemInfo systemInfo : systemInfoList) {
                creatorSystemIds.add(systemInfo.getId());
            }
            List<Integer> notInSystemIdList = new ArrayList<>();
            List<SystemInfo> notInSystemList = systemDao.selectList(new LambdaQueryWrapper<SystemInfo>()
                    .eq(SystemInfo::getIsVisible, false));
            for (SystemInfo e : notInSystemList){
                notInSystemIdList.add(e.getId());
            }
            queryWrapper.eq("is_visible", true)
                    .or()
                    .and(i -> i.in(!creatorSystemIds.isEmpty(), "system_id", creatorSystemIds)
                            .in(!systemIds.isEmpty(), "system_id", systemIds)
                            .notIn(!notInSystemIdList.isEmpty(), "system_id", notInSystemIdList));
        }
        if (modelInfo.getModelName() != null) {
            queryWrapper.like("model_name", modelInfo.getModelName());
        }
        if (modelInfo.getUserLevel() != null) {
            queryWrapper.like("user_level", modelInfo.getUserLevel());
        }
        if (modelInfo.getField() != null) {
            String[] fields = modelInfo.getField().split(",");
            queryWrapper.and(q -> {
                for (String field : fields) {
                    q.like("field", field).or();
                }
                return q;
            });
        }
        if (modelInfo.getServiceType() != null) {
            queryWrapper.like("service_type", modelInfo.getServiceType());
        }
        if (modelInfo.getModelType() != null) {
            queryWrapper.like("model_type", modelInfo.getModelType());
        }
        if (modelInfo.getSystemId() != 0) {
            queryWrapper.eq("system_id", modelInfo.getSystemId());
        }
        if (modelInfo.getUnit() != null) {
            queryWrapper.eq("unit", modelInfo.getUnit());
        }
        queryWrapper.like(modelInfo.getVague() != null, "CONCAT(describe, keyword, field" +
                ",unit)", modelInfo.getVague());
        if (modelInfo.getAssessmentCount() != 0) {
            queryWrapper.eq("assessment_count", modelInfo.getAssessmentCount());
        }
        if (modelInfo.getStatus() != null) {
            queryWrapper.eq("status", modelInfo.getStatus());
        }
        if (modelInfo.getDelete() != null) {
            queryWrapper.eq("delete", modelInfo.getDelete());
        }
        String sortFied = modelInfo.getSortField();
        String sortOrder = modelInfo.getSortOrder();
        if (sortFied != null && sortOrder != null) {
            if ("desc".equalsIgnoreCase(sortOrder)) {
                queryWrapper.orderByDesc(sortFied);
            } else {
                queryWrapper.orderByAsc(sortFied);
            }
        } else {
            queryWrapper.orderByDesc("id");
        }
            return modelDao.selectPage(page, queryWrapper);
        }



    @Override
    public void updateModel(ModelInfo modelInfo) {
        if (!userUtils.isAdmin()) {
            throw new RuntimeException("该用户无修改权限");
        }
        UpdateWrapper<ModelInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", modelInfo.getId());
        if (modelInfo.getModelName()!=null){
            updateWrapper.set("model_name", modelInfo.getModelName());
        }
        if (modelInfo.getUserLevel()!=null){
            updateWrapper.set("user_level", modelInfo.getUserLevel());
        }
        if (modelInfo.getField()!=null){
            updateWrapper.set("field", modelInfo.getField());
        }
        if (modelInfo.getServiceType()!=null){
            updateWrapper.set("service_type", modelInfo.getServiceType());
        }
        if (modelInfo.getUnit()!=null){
            updateWrapper.set("unit", modelInfo.getUnit());
        }
        if (modelInfo.getDescribe()!=null){
            updateWrapper.set("describe", modelInfo.getDescribe());
        }
        if (modelInfo.getKeyword()!=null){
            updateWrapper.set("keyword", modelInfo.getKeyword());
        }
        if (modelInfo.getAssessmentCount()!=0){
            updateWrapper.set("assessment_count", modelInfo.getAssessmentCount());
        }
        update(updateWrapper);
    }


    @Override
    public void deleteModels(List<Integer> ids) {
        modelDao.deleteBatchIds(ids);
    }


    @Override
    public boolean updateModelStatus(Long id, Boolean status) {
        return modelDao.updateStatusById(id, status)>0;
    }


    //TODO
    @Override
    @Transactional
    public ModelInfo createModel(ModelInfo modelInfo) {
        SystemInfo systemInfo = this.systemDao.selectById(modelInfo.getSystemId());
        modelInfo.setUnit(systemInfo.getUnit());
        modelInfo.setUserLevel(systemInfo.getUserLevel());
        modelInfo.setDelete(false);
        modelInfo.setStatus(true);
        modelInfo.setModelNameZh(modelInfo.getSign());
        modelInfo.setCreateTime(DateUtils.currentTimeDate());
        if (modelDao.insert(modelInfo)>0) {
            IndexSystemInfo indexSystemInfo = this.builderIsInfo(modelInfo);
            IndexSystemInfo insertIsInfo = this.indexSystemService.addIndexSystemInfo(indexSystemInfo);
            return modelInfo;
        } else {
            return null;
        }
    }


   /**
    * @Description 根据系统id删除其对应模型信息
    * @auther getao
    * @Date 2024/8/22 15:05
    * @Param [systemId]
    * @Return boolean
    */
    @Override
    @Transactional
    public boolean deleteModelBySystemId(int systemId) {
        /*UpdateWrapper<TbModelInfo> wrapper = new UpdateWrapper<>();
        wrapper.eq("system_id", systemId);
        this.modelDao.delete(wrapper);*/
        LambdaUpdateChainWrapper<ModelInfo> update = new LambdaUpdateChainWrapper<>(this.modelDao);
        boolean rs = update.eq(ModelInfo::getSystemId, systemId).set(ModelInfo::getDelete, true).update();
        return rs;
    }


   /**
    * @Description 根据模型id获取模型详情信息
    * @auther getao
    * @Date 2024/8/27 10:37
    * @Param [modelId]
    * @Return cn.iecas.simulate.assessment.entity.domain.TbModelInfo
    */
    @Override
    public ModelInfo getModelInfoById(int modelId) {
        ModelInfo modelInfo = this.modelDao.selectById(modelId);
        return modelInfo;
    }


    @Override
    public List<Map<String, Object>> getServiceTypeByType() {
        //原来返回的Map<String,Long>
//        List<TbModelInfo> models = modelDao.selectList(null);
//        return models.stream().collect(Collectors.groupingBy(TbModelInfo::getServiceType,Collectors.counting()));
        List<ModelInfo> models = modelDao.selectList(null);

        // 使用流进行分组和计数
        Map<String, Long> groupedCounts = models.stream()
                .collect(Collectors.groupingBy(ModelInfo::getServiceType, Collectors.counting()));

        // 转换为所需格式
        return groupedCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("Name", entry.getKey());
                    result.put("value", entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }


    @Override
    public List<String> findModelUnits() {
        List<String> modelUnits=modelDao.findModelUnits();
        return modelUnits;
    }


    @Override
    public void updateModelVisible(Long id, Boolean visible) {
        JSONObject userJsonInfoByToken = userUtils.getUserJsonInfoByToken();
        ModelInfo modelInfo = baseMapper.selectById(id);
        int systemId = modelInfo.getSystemId();
        Integer uid = systemDao.selectById(systemId).getUid();
        if (userJsonInfoByToken.getBoolean("is_admin") || userJsonInfoByToken.getBoolean("is_super_admin")
                || Long.parseLong(String.valueOf(userJsonInfoByToken.getInteger("id"))) == uid){
            LambdaUpdateChainWrapper<ModelInfo> updateChainWrapper = new LambdaUpdateChainWrapper<>(baseMapper);
            updateChainWrapper.eq(ModelInfo::getId, id).set(ModelInfo::getIsVisible, visible).update();
        }else{
            throw new CommonException("当前登录用户无修改权限");
        }
    }


    /**
     *  @author: getao
     *  @Date: 2024/11/11 14:53
     *  @Description: 更新模型信息
     */
    @Override
    public boolean updateByWrapper(Wrapper wrapper) {
        return this.update(wrapper);
    }


    /**
     * 获取模型类型
     * @return
     */
    @Override
    public Collection<String> getModelType() {
        List<String> modelTypes  = Lists.newArrayList("目标检测类", "业务处理类");
        return modelTypes;
    }

    /**
     * 模型标识唯一性校验
     * @param sign
     * @return
     */
    @Override
    public Boolean checkModelSign(String sign) {
        if (StringUtils.isBlank(sign))
            return false;
        QueryWrapper<ModelInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("sign", sign);
        List<ModelInfo> modelInfos = this.modelDao.selectList(wrapper);
        return modelInfos.size() == 0;
    }


    /**
     *  @author: getao
     *  @Date: 2024/11/18 11:27
     *  @Description: 获取模型业务类型信息
     */
    @Override
    public Collection<String> getServiceTypeFromModel() {
        QueryWrapper<ModelInfo> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT service_type");
        Set<String> services = this.modelDao.selectList(wrapper).stream().map(ModelInfo::getServiceType).map(e -> e.split(","))
                .flatMap(Arrays::stream).collect(Collectors.toSet());
        this.setMustServiceType(services);
        return services;
    }


    /**
     *  @author: getao
     *  @Date: 2024/11/13 14:41
     *  @Description: 获取模型领域信息
     */
    @Override
    public Collection<String> getFieldFromModel() {
        QueryWrapper<ModelInfo> wrapper = new QueryWrapper<>();
        wrapper.select("DISTINCT field");
        Set<String> fields = this.modelDao.selectList(wrapper).stream().map(ModelInfo::getField).map(e -> e.split(","))
                .flatMap(Arrays::stream).collect(Collectors.toSet());
        this.setMustField(fields);
        return fields;
    }


    private void setMustField(Set<String> fields) {
        fields.add("陆");
        fields.add("海");
        fields.add("空");
        fields.add("天");
    }


    private void setMustServiceType(Set<String> services) {
        services.add("分析研判");
        services.add("分发共享");
        services.add("融合处理");
        services.add("筹划");
    }

    private IndexSystemInfo builderIsInfo(ModelInfo modelInfo) {
        JSONObject indexInfos = modelInfo.getIndexInfos();
        String modelName = modelInfo.getModelName();
        String indexSystemName = modelName + "-评估指标体系";
        int modelId = modelInfo.getId();
        IndexSystemInfo indexSystemInfo = IndexSystemInfo.builder().indexInfos(indexInfos).modelName(modelName)
                .indexSystemName(indexSystemName).modelId(modelId).fromRegister(true).build();
        return indexSystemInfo;
    }


    /**
     * @Description 同步模型
     * @Author getao
     * @Date 14:07 2025/3/22
     * @Param [modelInfo]
     * @return cn.iecas.simulate.assessment.entity.domain.ModelInfo
     */
    @Override
    public ModelInfo syncModel(ModelInfo modelInfo) {
        modelInfo.setModelType("未适配");
        modelInfo.setField("未适配");
        modelInfo.setUnit("未适配");
        modelInfo.setUserLevel("未适配");
        modelInfo.setCreateTime(DateUtils.currentTimeDate());
        this.modelDao.insert(modelInfo);
        return modelInfo;
    }


    @Override
    public ModelTypeService getModelTypeServiceById(Integer modelId) {
        ModelInfo modelInfo = this.getModelInfoById(modelId);
        ModelType modelType = ModelType.valueOf(modelInfo.getModelType().toLowerCase(Locale.ROOT));
        ModelTypeService modelTypeService = modelType.getModelTypeService();
        return modelTypeService;
    }
}
