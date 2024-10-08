package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import java.util.List;
import java.util.Map;



/**
 * @auther getao
 * @Date 2024/8/23 15:06
 * @Description 仿真数据服务接口类
 */
public interface SimulateDataService {

    boolean insertBatch(List<SimulateDataInfo> dataInfos);

    PageResult<SimulateDataInfo> listSimulateData(SimulateDataInfoDto dataInfo);

    List<SimulateDataInfo> getSimulateDataByModel(int taskId, int modelId);

    Map<String, Long> getImportTrendByTaskId(Integer taskId);

}
