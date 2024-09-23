package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;
import java.util.Map;



/**
 * @auther chenyulin
 * @date 2024/8/19
 * @description 模型管理服务接口类
 */
public interface ModelService {

    IPage<TbModelInfo> getModelInfo(TbModelInfo tbModelInfo);

    void updateModel(TbModelInfo tbModelInfo);

    void deleteModels(List<Integer> ids);

    boolean updateModelStatus(Long id, Boolean status);

    boolean createModel(TbModelInfo tbModelInfo);

    boolean deleteModelBySystemId(int systemId);

    TbModelInfo getModelInfoById(int modelId);

    List<Map<String, Object>> getServiceTypeByType();

    List<String> findModelUnits();
}
