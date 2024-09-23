package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.dao.ModelDao;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.service.ModelService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * @auther cyl
 * @date 2024/8/19
 * @description 模型信息服务接口实现类
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelDao, TbModelInfo> implements ModelService {

    @Autowired
    private ModelDao modelDao;
    //查询模型信息


    @Override
    public IPage<TbModelInfo> getModelInfo(TbModelInfo tbModelInfo) {
        Page<TbModelInfo> page = new Page<>(tbModelInfo.getPageNo(), tbModelInfo.getPageSize());
        QueryWrapper<TbModelInfo> queryWrapper = new QueryWrapper<>();
        if(tbModelInfo.getModelName()!=null){
            queryWrapper.eq("model_name",tbModelInfo.getModelName());
        }
        if(tbModelInfo.getUserLevel()!=null){
            queryWrapper.eq("user_level",tbModelInfo.getUserLevel());
        }
        if(tbModelInfo.getField()!=null){
            queryWrapper.eq("field",tbModelInfo.getField());
        }
        if(tbModelInfo.getServiceType()!=null){
            queryWrapper.eq("service_type",tbModelInfo.getServiceType());
        }
        if(tbModelInfo.getSystemId() !=0){
            queryWrapper.eq("system_id",tbModelInfo.getSystemId());
        }
        if(tbModelInfo.getUnit()!=null){
            queryWrapper.eq("unit",tbModelInfo.getUnit());
        }
        queryWrapper.like(tbModelInfo.getVague() != null, "CONCAT(describe, keyword, field" +
                ",unit)",tbModelInfo.getVague());
        //模糊查询describe字段
//        if(tbModelInfo.getDescribe()!=null){
//            queryWrapper.like(true,"concat('%',describe,'%')",tbModelInfo.getDescribe());
//        }
//        //模糊查询keyword字段
//        if(tbModelInfo.getKeyword()!=null){
//            queryWrapper.like(true,"concat('%',keyword,'%')",tbModelInfo.getKeyword());
//        }
        if(tbModelInfo.getAssessmentCount() != 0){
            queryWrapper.eq("assessment_count",tbModelInfo.getAssessmentCount());
        }
        if(tbModelInfo.getStatus()!=null){
            queryWrapper.eq("status",tbModelInfo.getStatus());
        }
        if(tbModelInfo.getDelete()!=null){
            queryWrapper.eq("delete",tbModelInfo.getDelete());
        }
        String sortFied=tbModelInfo.getSortField();
        String sortOrder=tbModelInfo.getSortOrder();
        if(sortFied!=null && sortOrder!=null){
            if("desc".equalsIgnoreCase(sortOrder)){
                queryWrapper.orderByDesc(sortFied);
            }else{
                queryWrapper.orderByAsc(sortFied);
            }
        }else{
            queryWrapper.orderByDesc("id");
        }
            return modelDao.selectPage(page,queryWrapper);
        }


    @Override
    public void updateModel(TbModelInfo tbModelInfo) {
        UpdateWrapper<TbModelInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",tbModelInfo.getId());
        if (tbModelInfo.getModelName()!=null){
            updateWrapper.set("model_name",tbModelInfo.getModelName());
        }
        if (tbModelInfo.getUserLevel()!=null){
            updateWrapper.set("user_level",tbModelInfo.getUserLevel());
        }
        if (tbModelInfo.getField()!=null){
            updateWrapper.set("field",tbModelInfo.getField());
        }
        if (tbModelInfo.getServiceType()!=null){
            updateWrapper.set("service_type",tbModelInfo.getServiceType());
        }
        if (tbModelInfo.getUnit()!=null){
            updateWrapper.set("unit",tbModelInfo.getUnit());
        }
        if (tbModelInfo.getDescribe()!=null){
            updateWrapper.set("describe",tbModelInfo.getDescribe());
        }
        if (tbModelInfo.getKeyword()!=null){
            updateWrapper.set("keyword",tbModelInfo.getKeyword());
        }
        if (tbModelInfo.getAssessmentCount()!=0){
            updateWrapper.set("assessment_count",tbModelInfo.getAssessmentCount());
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
    public boolean createModel(TbModelInfo tbModelInfo) {
        return modelDao.insert(tbModelInfo)>0;
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
        LambdaUpdateChainWrapper<TbModelInfo> update = new LambdaUpdateChainWrapper<>(this.modelDao);
        boolean rs = update.eq(TbModelInfo::getSystemId, systemId).set(TbModelInfo::getDelete, true).update();
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
    public TbModelInfo getModelInfoById(int modelId) {
        TbModelInfo modelInfo = this.modelDao.selectById(modelId);
        return modelInfo;
    }


    @Override
    public List<Map<String, Object>> getServiceTypeByType() {
        //原来返回的Map<String,Long>
//        List<TbModelInfo> models = modelDao.selectList(null);
//        return models.stream().collect(Collectors.groupingBy(TbModelInfo::getServiceType,Collectors.counting()));
        List<TbModelInfo> models = modelDao.selectList(null);

        // 使用流进行分组和计数
        Map<String, Long> groupedCounts = models.stream()
                .collect(Collectors.groupingBy(TbModelInfo::getServiceType, Collectors.counting()));

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
}
