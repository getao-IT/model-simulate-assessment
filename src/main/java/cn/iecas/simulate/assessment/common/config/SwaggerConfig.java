package cn.iecas.simulate.assessment.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;



/**
 * @auther getao
 * @date 2024/8/16
 * @description swagger配置类
 */
@Configuration
public class SwaggerConfig {


    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("cn.iecas.simulate.assessment.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("模型仿真工具与跨域多级模型评估接口文档")
                .description("这是一个模型仿真工具与跨域多级模型评估接口文档")
                .license("Authorized by ge tao")
                .version("v1.0.0")
                .licenseUrl("http://localhost:8888//model-assessment/swagger-ui.html")
                .build();
    }
}
