package cn.iecas.simulate.assessment;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import springfox.documentation.swagger2.annotations.EnableSwagger2;



@EnableAsync
@EnableCaching
@EnableSwagger2
@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan
public class SimulateAssessmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimulateAssessmentApplication.class, args);
    }

}
