package cn.iecas.simulate.assessment.runner;

import cn.iecas.simulate.assessment.entity.domain.SceneInfo;
import cn.iecas.simulate.assessment.entity.domain.SystemInfo;
import cn.iecas.simulate.assessment.entity.domain.TbModelInfo;
import cn.iecas.simulate.assessment.service.ModelService;
import cn.iecas.simulate.assessment.service.SceneService;
import cn.iecas.simulate.assessment.service.SystemService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import javax.sql.DataSource;
import java.util.Date;



@Order(1)
@Data
@Configuration
@Slf4j
@ConditionalOnProperty(value = "assessment.init.integrated", havingValue = "true")
@ConfigurationProperties(prefix = "assessment.init")
public class InitIntegratedRunner<T> implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SystemService systemService;

    @Autowired
    private ModelService modelService;

    @Autowired
    private SceneService sceneService;

    private String systemSign;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        ScriptRunner runner = new ScriptRunner(dataSource.getConnection());
        runner.setAutoCommit(false);
        runner.setStopOnError(false);
        runner.setThrowWarning(false);
        runner.setFullLineDelimiter(false);
        runner.setEscapeProcessing(true);
        runner.setDelimiter(";");
        runner.setLogWriter(null);

        /*SqlRunner sqlRunner = new SqlRunner(dataSource.getConnection());
        String deletesql = "DELETE FROM tb_system_info WHERE\tsystem_sign = " + "'" + systemSign + "';";
        sqlRunner.delete(deletesql);*/
        // 28所模型集成初始化
        int systemId1 = this.initInfoSystem("中国电科28所业务模型系统", "zgdkSYWMXXT", "中国电子科技集团28所");
        this.initModelInfo1(systemId1);
        // 空天院模型集成初始化
        int systemId2 = this.initInfoSystem("空天院模型服务系统", "ktyMXFWXT", "中国科学院空天信息创新研究院");
        this.initModelInfo2(systemId2);
        this.initSecenceInfo();
        log.info("测试集成数据初始化完成...");
    }


    public int initInfoSystem(String systemName, String systemSing, String unit) {
        SystemInfo systemInfo = SystemInfo.builder().delete(false).importTime(new Date()).isVisible(false).modelTotal(32).status(false).systemIp("192.168.10.1")
                .systemName(systemName).systemSign(systemSing).uid(0).unit(unit).userLevel("军委")
                .username("system").build();
        this.systemService.saveSystemInfo(systemInfo);
        return systemInfo.getId();
    }


    private void initModelInfo1(int systemId) {
        TbModelInfo mfhfx = TbModelInfo.builder().assessmentCount(0).delete(false).describe("这是一个名称为\"府会关系分析模型\"的业务模型，用以分析美府会关系。")
                .field("陆,海,空,天").isVisible(false).modelName("府会关系分析模型").modelNameZh("mfhfx").serviceType("分析研判")
                .sign("mfhfx").status(false).systemId(systemId).unit("中国电子科技集团28所").userLevel("军委").version("v1.0.0").build();
        this.modelService.save(mfhfx);
        TbModelInfo zmdb = TbModelInfo.builder().assessmentCount(0).delete(false).describe("这是一个名称为\"中美实力力量对比评估模型\"的业务模型，用以对比中美各领域的实际实力。")
                .field("陆,海,空,天").isVisible(false).modelName("中美实力力量对比评估模型").modelNameZh("zmdb").serviceType("分析研判")
                .sign("zmdb").status(false).systemId(systemId).unit("中国电子科技集团28所").userLevel("军委").version("v1.0.0").build();
        this.modelService.save(zmdb);
        TbModelInfo fjjcys = TbModelInfo.builder().assessmentCount(0).delete(false).describe("这是一个名称为\"中美实力力量对比评估模型\"的业务模型，用以对比中美各领域的实际实力。")
                .field("陆,海,空,天").isVisible(false).modelName("中美实力力量对比评估模型").modelNameZh("zmdb").serviceType("分析研判")
                .sign("zmdb").status(false).systemId(systemId).unit("中国电子科技集团28所").userLevel("军委").version("v1.0.0").build();
        this.modelService.save(zmdb);
    }


    private void initModelInfo2(int systemId) {
        TbModelInfo fjjcys = TbModelInfo.builder().assessmentCount(0).delete(false).describe("这是一个名称为\"飞机检测演示\"的目标检测模型，用以图像飞机目标检测。")
                .field("陆,海,空,天").isVisible(false).modelName("飞机检测演示").modelNameZh("fjjcys").serviceType("分析研判")
                .sign("fjjcys").status(false).systemId(systemId).unit("中国科学院空天信息创新研究院").userLevel("军委").version("v1.0.0").build();
        this.modelService.save(fjjcys);
        TbModelInfo clmbjc = TbModelInfo.builder().assessmentCount(0).delete(false).describe("这是一个名称为\"车辆目标检测\"的目标检测模型，用以图像车辆目标检测。")
                .field("陆,海,空,天").isVisible(false).modelName("车辆目标检测").modelNameZh("clmbjc").serviceType("分析研判")
                .sign("clmbjc").status(false).systemId(systemId).unit("中国科学院空天信息创新研究院").userLevel("军委").version("v1.0.0").build();
        this.modelService.save(clmbjc);
    }


    private void initSecenceInfo() {
        SceneInfo lhdy = SceneInfo.builder().createTime(new Date()).creater("system").delete(false)
                .describe("这是一个名称为\"联合岛屿攻击\"的通用作战场景。").field("分析研判,分发共享,融合处理,筹划").keyword("联合岛屿攻击,通用")
                .sceneName("联合岛屿攻击").userLevel("军委,战区,军兵种,一线信息系统,现场任务").build();
        this.sceneService.addSceneInfo(lhdy);
        SceneInfo lhhs = SceneInfo.builder().createTime(new Date()).creater("system").delete(false)
                .describe("这是一个名称为\"联合海上机动\"的通用作战场景。").field("分析研判,分发共享,融合处理,筹划").keyword("联合海上机动,通用")
                .sceneName("联合海上机动").userLevel("军委,战区,军兵种,一线信息系统,现场任务").build();
        this.sceneService.addSceneInfo(lhhs);
        SceneInfo lhbj = SceneInfo.builder().createTime(new Date()).creater("system").delete(false)
                .describe("这是一个名称为\"联合边境区域防卫\"的通用作战场景。").field("分析研判,分发共享,融合处理,筹划").keyword("联合边境区域防卫,通用")
                .sceneName("联合边境区域防卫").userLevel("军委,战区,军兵种,一线信息系统,现场任务").build();
        this.sceneService.addSceneInfo(lhbj);
        SceneInfo lhfk = SceneInfo.builder().createTime(new Date()).creater("system").delete(false)
                .describe("这是一个名称为\"联合防空反导\"的通用作战场景。").field("分析研判,分发共享,融合处理,筹划").keyword("联合防空反导,通用")
                .sceneName("联合防空反导").userLevel("军委,战区,军兵种,一线信息系统,现场任务").build();
        this.sceneService.addSceneInfo(lhfk);
        SceneInfo lhjs = SceneInfo.builder().createTime(new Date()).creater("system").delete(false)
                .describe("这是一个名称为\"联合监视\"的通用作战场景。").field("分析研判,分发共享,融合处理,筹划").keyword("联合监视,通用")
                .sceneName("联合监视").userLevel("军委,战区,军兵种,一线信息系统,现场任务").build();
        this.sceneService.addSceneInfo(lhjs);
    }
}
