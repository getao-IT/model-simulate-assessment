package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.domain.IndexInfo;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;
import java.util.Map;



/**
 * @auther getao
 * @Date 2024/8/29 16:40
 * @Description 模型指标信息服务接口类
 */
public interface IndexInfoService {

    JSONObject getIndexBySignAndBatchNo(String sign, int batchNo);

    Map<String,Object> getIndexInfo(String sign);

    IndexInfo insert(IndexInfo indexInfo);

    List<Map<String, Object>> getIndexInfoByLevel(int modelId, int batchNo);

    List<IndexInfo> getIndexInfoByQuery(QueryWrapper<IndexInfo> queryWrapper);

    IndexInfo getIndexInfoById(int id);

    IndexInfo updateIndexInfoById(IndexInfo indexInfo);
}
