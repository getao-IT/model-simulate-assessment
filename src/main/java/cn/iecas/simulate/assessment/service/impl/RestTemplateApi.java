package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;



/**
 * @auther getao
 * @Date 2024/8/23 14:19
 * @Description 三方接口API
 */
@Slf4j
@Component
public class RestTemplateApi {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${value.api.get-simulate-data}")
    private String getSimulateDataUrl;


    public JSONObject getSimulateData(SimulateTaskInfoDto taskInfoDto) {
        HttpHeaders headers = new HttpHeaders();
        JSONObject params = new JSONObject();
        params.put("modelName", taskInfoDto.getModelName());
        params.put("pageSize", taskInfoDto.getPageSize());
        params.put("pageNum", taskInfoDto.getPageNo());
        if (taskInfoDto.getProposalTimeLess() != null) {
            params.put("proposalTimeLess", taskInfoDto.getProposalTimeLess());
        }
        if (taskInfoDto.getProposalTimeGre() != null) {
            params.put("proposalTimeGre", taskInfoDto.getProposalTimeGre());
        }
        if (taskInfoDto.getTerritory() != null) {
            params.put("territory", taskInfoDto.getTerritory());
        }
        HttpEntity<JSONObject> entity = new HttpEntity(params, headers);
        try {
            JSONObject result = restTemplate.exchange(getSimulateDataUrl, HttpMethod.POST, entity, JSONObject.class).getBody();
            if (result.getBoolean("success")) {
                return result;
            } else {
                log.error("三方接口 {} 报错...", getSimulateDataUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", getSimulateDataUrl, e.getMessage());
            throw e;
        }
    }
}


//{
//"data": {
//        "firstIndex": ["一级指标1","一级指标2","一级指标3"],
//        "otherIndex": {
//        "二级指标1": [
//        {"三级指标1": ["四级指标1","四级指标2","四级指标3"],
//        "三级指标2": ["四级指标1","四级指标2","四级指标3"]}
//        ],
//        "二级指标2": [
//        {"三级指标1": ["四级指标1","四级指标2","四级指标3"],
//        "三级指标2": ["四级指标1","四级指标2","四级指标3"]}
//        ]
//        }
//        },
//        "code": null,
//        "message": "获取模型指标信息成功"
//}