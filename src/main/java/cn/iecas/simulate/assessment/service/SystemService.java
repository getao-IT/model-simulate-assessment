package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.SystemInfo;
import cn.iecas.simulate.assessment.entity.dto.SystemInfoDto;
import java.util.List;
import java.util.Map;


/**
 * @auther getao
 * @Date 2024/8/21 15:08
 * @Description 信息系统服务接口类
 */
public interface SystemService {

    PageResult<SystemInfo> getSystemInfo(SystemInfoDto systemInfoDto);

    SystemInfo saveSystemInfo(SystemInfo systemInfo);

    SystemInfo updateSystemInfo(SystemInfo systemInfo);

    Integer batchDeleteSystemInfo(List<Integer> idList);

    List<Map<String,String>> findUserLevels();

    boolean updateModelStatus(Long id, Boolean status);

    List<Integer> findSystemStatus();
}
