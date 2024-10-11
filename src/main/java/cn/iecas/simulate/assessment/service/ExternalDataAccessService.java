package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;

import java.util.List;
import java.util.Map;


public interface ExternalDataAccessService {

    /**
     * 根据外部接口地址及查询参数引接数据
     */
    List<SimulateDataInfo> getExternalData(ExternalDataDTO dto) throws Exception;


    /**
     * 异步数据引接
     */
    Map<String, Object> startTask(ExternalDataDTO dto);


    /**
     * 终止线程
     * @param threadName 线程名称
     */
    Map<String, Object> stopTask(String threadName, Integer taskId);


    /**
     * 挂起线程
     * @param threadName 线程名称
     */
    Map<String, Object> suspendTask(String threadName, Integer taskId) throws InterruptedException;


    /**
     * 恢复线程
     * @param threadName 线程名称
     */
    Map<String, Object> resumeTask(String threadName, Integer taskId, Integer frequency, Integer pageSize) throws Exception;
}
