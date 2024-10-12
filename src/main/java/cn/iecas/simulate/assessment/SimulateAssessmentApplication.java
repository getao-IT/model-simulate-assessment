package cn.iecas.simulate.assessment;


import cn.iecas.simulate.assessment.service.SimulateTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
public class SimulateAssessmentApplication implements CommandLineRunner {

    @Autowired
    SimulateTaskService simulateTaskService;


    public static void main(String[] args) {
        SpringApplication.run(SimulateAssessmentApplication.class, args);
    }


    /**
     * 重启服务的时候，检测task表中不为WAIT和FINSIH的全部标记为FAIL
     */
    private void init(){
        simulateTaskService.checkStatusAndSetFail();
    }


    @Override
    public void run(String... args) throws Exception {
        init();
    }
}
