package cn.iecas.simulate.assessment.util;

import cn.iecas.simulate.assessment.entity.common.CommonResult;
import cn.iecas.simulate.assessment.entity.common.ResultCodeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;



@Slf4j
@Component
public class UserUtils {

    @Value("${value.user-api.user-info}")
    private String userInfoApi;

    @Value(value = "${value.user-api.user-query-name}")
    private String queryUserNameUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest httpRequest;


    public Map<Integer,String> getAllUserInfo(){
        String token = httpRequest.getHeader("token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        Map<Integer,String> userIdNameMap = new HashMap<>();
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        JSONObject allUserInfo = restTemplate.exchange(queryUserNameUrl, HttpMethod.GET,httpEntity,JSONObject.class).getBody();
        JSONArray userInfoJSONArray = allUserInfo.getJSONArray("data");
        for (int index = 0; index < userInfoJSONArray.size(); index++) {
            JSONObject userInfo = userInfoJSONArray.getJSONObject(index);
            userIdNameMap.put(userInfo.getInteger("id"),userInfo.getString("name"));
        }
        return userIdNameMap;
    }


    /**
     *  @author: getao
     *  @Date: 2024/10/24 17:20
     *  @Description: 根据用户id获取用户信息
     */
    public Map<Integer,String> getUserInfoById(Set<Integer> userIdSet) {
        String token = httpRequest.getHeader("token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("user_ids",userIdSet.toString());
        Map<Integer,String> userIdNameMap = new HashMap<>();
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        JSONObject allUserInfo = restTemplate.exchange(queryUserNameUrl, HttpMethod.GET,httpEntity,JSONObject.class,paramMap).getBody();
        JSONArray userInfoJSONArray = allUserInfo.getJSONArray("data");
        for (int index = 0; index < userInfoJSONArray.size(); index++) {
            JSONObject userInfo = userInfoJSONArray.getJSONObject(index);
            userIdNameMap.put(userInfo.getInteger("id"),userInfo.getString("name"));
        }
        return userIdNameMap;
    }


    /**
     *  @author: getao
     *  @Date: 2024/10/24 17:20
     *  @Description: 通过token获取用户信息
     */
    public CommonResult<JSONObject> getUserInfoByToken(String token) throws ResourceAccessException {
        log.info("开始验证token");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("token",token);
        HttpEntity<String> httpEntity = new HttpEntity<>(null,httpHeaders);
        CommonResult<JSONObject> result = new CommonResult();
        try {
            JSONObject jsonResult = restTemplate.exchange(userInfoApi, HttpMethod.GET,httpEntity, JSONObject.class).getBody();
            if (jsonResult.getInteger("code") == 0) {
                result.setCode(ResultCodeEnum.SUCCESS);
            } else {
                result.setCode(ResultCodeEnum.FAIL);
            }
            result.setData(jsonResult.getJSONObject("data"));
            result.setMessage(jsonResult.getString("msg"));
        }catch (ResourceAccessException e){
            log.error("访问用户信息接口：{} 超时",userInfoApi);
            throw new ResourceAccessException("用户服务访问出错");
        }
        return result;
    }


    /**
     *  @author: getao
     *  @Date: 2024/10/24 17:48
     *  @Description: 获取当前用户的token
     */
    public String getUserToken() {
        return httpRequest.getHeader("token");
    }


    /**
     *  @author: getao
     *  @Date: 2024/10/24 17:48
     *  @Description: 获取当前用户的token
     */
    public int getUserIdByToken() {
        String token = httpRequest.getHeader("token");
        CommonResult<JSONObject> userResult = getUserInfoByToken(token);
        if (userResult.getCode().equalsIgnoreCase("0")) {
            JSONObject userInfo = userResult.getData();
            return userInfo.getInteger("id");
        } else {
            return -1;
        }
    }


    /**
     *  @author: guoxun
     *  @Date: 2024/10/28 10:33
     *  @Description: 获取当前用户信息，通过token
     */
    public JSONObject getUserJsonInfoByToken(){
        String token = this.getUserToken();
        if (!StringUtils.hasLength(token)){
            throw new RuntimeException("token不存在!");
        }
        return this.getUserInfoByToken(token).getData();
    }
}
