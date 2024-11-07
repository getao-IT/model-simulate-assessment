package cn.iecas.simulate.assessment.service.model;


import cn.iecas.simulate.assessment.service.impl.ExternalDataAccessServiceImpl;

import java.util.List;


/**
 * @auther getao
 * @date 2024/10/30
 * @description 模型类型公共服务类
 */
public interface ModelTypeService<T> {

    List<T> requestUrl(Object params) throws Exception;

    void handleExternalData(List<T> externalDataJson, String threadName, Integer offset, ExternalDataAccessServiceImpl.StatusInfo info
            , Integer taskId, Integer modelId);
}
