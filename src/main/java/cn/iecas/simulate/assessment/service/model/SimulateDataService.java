package cn.iecas.simulate.assessment.service.model;

import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SimulateDataInfo;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;



/**
 * @auther getao
 * @Date 2024/8/23 15:06
 * @Description 仿真数据服务接口类
 */
public interface SimulateDataService<T> extends IService<T> {

    boolean insertBatch(List<T> dataInfos);

    PageResult<T> listSimulateData(SimulateDataInfoDto dataInfo);

    List<T> getSimulateDataByModel(int taskId, int modelId);

    Map<String, Long> getImportTrendByTaskId(Integer taskId);

    void updateDataInTheTask(Integer taskId, Integer modelId);
}
