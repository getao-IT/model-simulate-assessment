package cn.iecas.simulate.assessment.service;

import cn.iecas.simulate.assessment.entity.domain.IndexInfo;
import com.alibaba.fastjson.JSONObject;

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
}
