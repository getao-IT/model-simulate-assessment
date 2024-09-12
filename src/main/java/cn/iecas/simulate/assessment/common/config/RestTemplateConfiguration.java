package cn.iecas.simulate.assessment.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;



/**
 * 设置resttemplate超时时间
 */
@Configuration
public class RestTemplateConfiguration {


    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3 * 1000);
        httpRequestFactory.setConnectTimeout(4 * 1000);
        httpRequestFactory.setReadTimeout(10 * 60 * 1000);
        return new RestTemplate(httpRequestFactory);
    }
}
