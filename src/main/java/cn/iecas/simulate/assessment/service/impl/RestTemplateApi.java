package cn.iecas.simulate.assessment.service.impl;

import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.entity.dto.SimulateDataInfoDto;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import java.lang.instrument.Instrumentation;


/**
 * @auther getao
 * @Date 2024/8/23 14:19
 * @Description 三方接口API
 */
@Slf4j
@Component
public class RestTemplateApi {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${value.model-api.get-simulate-data}")
    private String getSimulateDataUrl;

    @Value("${value.model-api.getAllIndexIndicatorTask}")
    private String getAllIndexIndicatorTaskUrl;

    @Value("${value.model-api.queryZbCompare}")
    private String queryZbCompareUrl;

    @Value("${value.model-api.getSimulateRealData}")
    private String getSimulateRealDataUrl;

    @Value("${value.model-api.pullSimulateData}")
    private String pullSimulateDataUrl;


    /**
     * @Description 获取模型仿真数据
     * @auther getao
     * @Date 2024/10/30 10:47
     * @Param [taskInfoDto]
     * @Return com.alibaba.fastjson.JSONObject
     */
    public JSONObject getSimulateData(SimulateTaskInfoDto taskInfoDto) {
        HttpHeaders headers = new HttpHeaders();
        JSONObject params = new JSONObject();
        params.put("model", taskInfoDto.getModelNameZh());
        params.put("modelName", taskInfoDto.getModelNameZh());
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


    /**
     * @Description 获取中美对比模型对比方向信息
     * @auther getao
     * @Date 2024/10/30 10:49
     * @Param [modelType]
     * @Return com.alibaba.fastjson.JSONObject
     */
    public JSONObject getAllIndexIndicatorTask(ExternalDataDTO dataDTO) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<JSONObject> entity = new HttpEntity(null, headers);
        try {
            UriComponents url = UriComponentsBuilder.fromHttpUrl(getAllIndexIndicatorTaskUrl)
                    .queryParam("modeltype", dataDTO.getModelType()).queryParam("pageSize", dataDTO.getPageSize())
                    .queryParam("pageNum", dataDTO.getPageNo()).build().encode();
            JSONObject result = restTemplate.exchange(url.toUri(), HttpMethod.GET, entity, JSONObject.class).getBody();
            if (result.getBoolean("success")) {
                return result;
            } else {
                log.error("三方接口 {} 报错...", getAllIndexIndicatorTaskUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", getAllIndexIndicatorTaskUrl, e.getMessage());
            throw e;
        }
    }


    /**
     * @Description 获取中美模型对比某方向的具体评估信息
     * @auther getao
     * @Date 2024/10/30 11:05
     * @Param [taskId, countryCn]
     * @Return com.alibaba.fastjson.JSONObject
     */
    public JSONObject queryZbCompare(int taskId, String countryCn) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<JSONObject> entity = new HttpEntity(null, headers);
        try {
            UriComponents url = UriComponentsBuilder.fromHttpUrl(queryZbCompareUrl).queryParam("taskId", taskId)
                    .queryParam("countryCn", countryCn).build().encode();
            JSONObject result = restTemplate.exchange(url.toUri(), HttpMethod.GET, entity, JSONObject.class).getBody();
            if (result.getBoolean("success")) {
                return result;
            } else {
                log.error("三方接口 {} 报错...", queryZbCompareUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", queryZbCompareUrl, e.getMessage());
            throw e;
        }
    }


    /**
     *  @author: getao
     *  @Date: 2024/12/19 9:28
     *  @Description: 获取模型真实数据
     */
    public JSONObject getSimulateRealData(TbModelInfo modelInfo) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<JSONObject> entity = new HttpEntity(modelInfo, headers);
        try {
            JSONObject result = restTemplate.exchange(getSimulateRealDataUrl, HttpMethod.POST, entity, JSONObject.class).getBody();
            if (result.getBoolean("success")) {
                return result;
            } else {
                log.error("三方接口 {} 报错...", getSimulateRealDataUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", getSimulateRealDataUrl, e.getMessage());
            SimulateTaskInfoDto taskInfoDto = new SimulateTaskInfoDto();
            taskInfoDto.setModelNameZh("mfhfx");
            JSONObject simulateData = this.getSimulateData(taskInfoDto);
            return simulateData;
        }
    }


    /**
     *  @author: getao
     *  @Date: 2024/12/19 15:11
     *  @Description: 采集仿真数据
     */
    public JSONObject pullSimulateData(TbModelInfo modelInfo) {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<JSONObject> entity = new HttpEntity(modelInfo, headers);
        try {
            JSONObject result = restTemplate.exchange(pullSimulateDataUrl, HttpMethod.POST, entity, JSONObject.class).getBody();
            if (result.getBoolean("success")) {
                return result;
            } else {
                log.error("三方接口 {} 报错...", pullSimulateDataUrl);
                return null;
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", pullSimulateDataUrl, e.getMessage());
            ExternalDataDTO dataDTO = new ExternalDataDTO();
            dataDTO.setPageSize(10);
            dataDTO.setPageNum(1);
            dataDTO.setPageNo(1);
            JSONObject simulateData = this.getAllIndexIndicatorTask(dataDTO);
            return simulateData;
        }
    }
}

