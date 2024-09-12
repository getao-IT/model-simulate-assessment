package cn.iecas.simulate.assessment.common.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * @author vanishrain
 */
@Configuration
@MapperScan(value = {"cn.iecas.simulate.assessment.dao"})
public class MybatisPlusConfig {


    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //paginationInterceptor.setDialectType("postgresql");
        return paginationInterceptor;
    }
}
