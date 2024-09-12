package cn.iecas.simulate.assessment.service;


import cn.iecas.simulate.assessment.entity.common.PageResult;
import cn.iecas.simulate.assessment.entity.domain.IndexSystemInfo;
import cn.iecas.simulate.assessment.entity.dto.IndexSystemInfoDto;
import java.util.List;
import java.util.Map;



/**
 * @auther chenyulin
 * @date 2024/8/23
 * @description 指标体系服务接口类
 */
public interface IndexSystemService {

    void addIndexSystemInfo(IndexSystemInfo indexSystemInfo);

    void updateIndexSystemInfo(IndexSystemInfo indexSystemInfo);

    void deleteIndexSystemInfo(List<Integer> ids);

    void restore(String sqlFileName);

    IndexSystemInfo getById(int systemId);

    List<String> getIndexList(Integer id);

    List<Map<String, List<String>>> getIndexesByModelId(Integer modelId);

    PageResult<IndexSystemInfo> getIndexSystemInfo(IndexSystemInfoDto indexSystemInfoDto);
}
