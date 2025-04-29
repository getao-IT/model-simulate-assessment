package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.domain.ModelInfo;
import cn.iecas.simulate.assessment.service.assessment.ModelTypeService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collection;
import java.util.List;
import java.util.Map;



/**
 * @auther chenyulin
 * @date 2024/8/19
 * @description 模型管理服务接口类
 */
public interface ModelService extends IService<ModelInfo> {

    IPage<ModelInfo> getModelInfo(ModelInfo modelInfo);

    void updateModel(ModelInfo modelInfo);

    void deleteModels(List<Integer> ids);

    boolean updateModelStatus(Long id, Boolean status);

    ModelInfo createModel(ModelInfo modelInfo);

    boolean deleteModelBySystemId(int systemId);

    ModelInfo getModelInfoById(int modelId);

    List<Map<String, Object>> getServiceTypeByType();

    List<String> findModelUnits();

    void updateModelVisible(Long id, Boolean visible);

    boolean updateByWrapper(Wrapper wrapper);

    Collection<String> getFieldFromModel();

    Collection<String> getServiceTypeFromModel();

    Boolean checkModelSign(String sign);

    Collection<String> getModelType();

    ModelTypeService getModelTypeServiceById(Integer modelId);

    ModelInfo syncModel(ModelInfo modelInfo);
}
