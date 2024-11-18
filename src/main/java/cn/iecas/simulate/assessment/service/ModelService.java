package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
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
public interface ModelService extends IService<TbModelInfo> {

    IPage<TbModelInfo> getModelInfo(TbModelInfo tbModelInfo);

    void updateModel(TbModelInfo tbModelInfo);

    void deleteModels(List<Integer> ids);

    boolean updateModelStatus(Long id, Boolean status);

    boolean createModel(TbModelInfo tbModelInfo);

    boolean deleteModelBySystemId(int systemId);

    TbModelInfo getModelInfoById(int modelId);

    List<Map<String, Object>> getServiceTypeByType();

    List<String> findModelUnits();

    void updateModelVisible(Long id, Boolean visible);

    boolean updateByWrapper(Wrapper wrapper);

    Collection<String> getFieldFromModel();

    Collection<String> getServiceTypeFromModel();
}
