package cn.iecas.simulate.assessment.service.impl;

import cn.aircas.utils.file.FileUtils;
import cn.iecas.simulate.assessment.entity.domain.ModelInfo;
import cn.iecas.simulate.assessment.entity.domain.ModelRunlDataInfo;
import cn.iecas.simulate.assessment.entity.dto.ExternalDataDTO;
import cn.iecas.simulate.assessment.entity.dto.SimulateTaskInfoDto;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
import java.io.File;
import java.net.URI;
import java.util.List;


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

    @Value("${value.model-api.getSamplesetInfo}")
    private String getSamplesetInfoUrl;

    @Value("${value.model-api.getRealData}")
    private String getRealDataUrl;

    @Value("${value.model-api.getSimulateDt}")
    private String getSimulateDtUrl;

    @Value("${value.model-api.getProcessData}")
    private String getProcessDataUrl;

    @Value("${assessment.sys-sign}")
    private String searchParam;


    @Value("${assessment.detection.defaultOutput}")
    private String defaultOutput;

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
    public JSONObject getSimulateRealData(ModelInfo modelInfo) {
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
    public JSONObject pullSimulateData(ModelInfo modelInfo) {
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


    /**
     *  @author: getao
     *  @Date: 2025/03/11 15:30
     *  @Description: 获取样本信息
     */
    public JSONObject getSamplesetInfo(int pageNo, int pageSize, String classification, String category) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", request.getHeader("token"));
        HttpEntity<JSONObject> entity = new HttpEntity(null, headers);
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(getSamplesetInfoUrl).queryParam("pageNo", pageNo)
                    .queryParam("pageSize", pageSize).queryParam("classification", classification).queryParam("category", category);
            if (StringUtils.isNotBlank(searchParam))
                uriBuilder.queryParam("searchParam", searchParam);
            URI uri = uriBuilder.build().toUri();
            JSONObject result = restTemplate.exchange(uri, HttpMethod.GET, entity, JSONObject.class).getBody();
            if (result.getJSONObject("data") != null) {
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", getSamplesetInfoUrl, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取模型运行真实数据
     * @return
     */
    public JSONObject getRealDataFromModel(int taskId, int modelId, String inputPath, String outputPath, String way, Integer pageSize) {
        HttpHeaders headers = new HttpHeaders();
        //headers.add("token", request.getHeader("token"));
        HttpEntity<JSONObject> entity = new HttpEntity(null, headers);
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(getRealDataUrl).queryParam("inputPath", inputPath)
                    .queryParam("outputPath", outputPath).queryParam("taskId", taskId)
                    .queryParam("modelId", modelId).queryParam("pageSize", pageSize)
                    .queryParam("way", way).build().toUri();
            JSONObject result = restTemplate.exchange(uri, HttpMethod.GET, entity, JSONObject.class).getBody();
            if (result.getString("code").equalsIgnoreCase("OK")) {
                return result;
            } else {
                log.error("调用三方接口 {} 执行失败 ...", getRealDataUrl);
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", getRealDataUrl, e.getMessage());
        }
        return null;
    }


    /**
     * @Description 获取模型仿真数据，即模型输出数据
     * @Author getao
     * @Date 16:26 2025/3/13
     * @Param [path, inputPaths, number]
     * @return com.alibaba.fastjson.JSONObject
     */
    public JSONObject getSimulateDtFromModel(String path, List<String> inputPaths, Integer number) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", request.getHeader("token"));
        HttpEntity<JSONObject> entity = new HttpEntity(null, headers);
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(getSimulateDtUrl).queryParam("path", path)
                    .queryParam("inputPaths", inputPaths).queryParam("number", number).build().toUri();
            JSONObject result = restTemplate.exchange(uri, HttpMethod.GET, entity, JSONObject.class).getBody();
            if (result.getString("code").equalsIgnoreCase("OK")) {
                return result.getJSONObject("data");
            } else {
                log.error("调用三方接口 {} 执行失败 ...", getSimulateDtUrl);
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", getSimulateDtUrl, e.getMessage());
        }
        return null;
    }


    /**
     * 获取模型处理过程数据
     * @return
     */
    public JSONObject getProcessFromModel(String path, Integer number) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", request.getHeader("token"));
        HttpEntity<JSONObject> entity = new HttpEntity(null, headers);
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(getProcessDataUrl).queryParam("path", path)
                    .queryParam("number", number).build().toUri();
            JSONObject result = restTemplate.exchange(uri, HttpMethod.GET, entity, JSONObject.class).getBody();
            if (result.getString("code").equalsIgnoreCase("OK")) {
                return result.getJSONObject("data");
            } else {
                log.error("调用三方接口 {} 执行失败 ...", getProcessDataUrl);
            }
        } catch (Exception e) {
            log.error("调用三方接口 {} 报错，异常信息 {} ...", getProcessDataUrl, e.getMessage());
        }
        return null;
    }


    /**
     * @Description 调用智能服务
     * @auther getao
     * @Date 2024/10/30 10:49
     * @Param [modelType]
     * @Return com.alibaba.fastjson.JSONObject
     */
    public JSONObject callService(String callServiceUrl, String serviceName, String inputFile, String outputFile) {
        HttpHeaders headers = new HttpHeaders();
        try {
            JSONObject params = new JSONObject();
            params.put("service_name", serviceName);
            params.put("input_file", inputFile);
            params.put("output_file", outputFile);
            HttpEntity<JSONObject> entity = new HttpEntity(params, headers);
            //JSONObject result = restTemplate.exchange(callServiceUrl, HttpMethod.POST, entity, JSONObject.class).getBody();
            JSONObject result = new JSONObject();
            if (result.getInteger("data") == 0) {
                return result;
            } else {
                log.error("==>> 三方接口智能服务 {} 报错...", callServiceUrl);
                result.put("message", "智能服务接口报错");
                result.put("data", FileUtils.getStringPath(callServiceUrl, new File(inputFile).getName()));
                return result;
            }
        } catch (Exception e) {
            log.error("==>> 三方接口智能服务 {} 报错...，异常信息 {} ...", callServiceUrl, e.getMessage());
            throw e;
        }
    }


    /**
     * @Description 调用模型评估服务
     * @Author getao
     * @Date 11:12 2025/3/18
     * @Param [assessmentApi, modelName, inputPath, samplePath]
     * @return com.alibaba.fastjson.JSONObject
     */
    public JSONObject assessmentService(String assessmentApi, String inputPath, String samplePath) {
        HttpHeaders headers = new HttpHeaders();
        try {
            JSONObject params = new JSONObject();
            HttpEntity<JSONObject> entity = new HttpEntity(null, headers);
            URI uri = UriComponentsBuilder.fromHttpUrl(assessmentApi).queryParam("predPath", inputPath)
                    .queryParam("gtPath", samplePath).build().encode().toUri();
            JSONObject result = restTemplate.exchange(uri, HttpMethod.GET, entity, JSONObject.class).getBody();
            if (result.getInteger("status") == 200) {
                return result;
            } else {
                log.error("==>> 三方接口模型评估服务 {} 报错...", assessmentApi);
                result.put("message", "模型评估服务接口报错");
                result.put("data", FileUtils.getStringPath(assessmentApi, new File(inputPath).getName()));
                return result;
            }
        } catch (Exception e) {
            log.error("==>> 三方接口模型评估服务 {} 报错...，异常信息 {} ...", assessmentApi, e.getMessage());
            throw e;
        }
    }
}

