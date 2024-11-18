package cn.iecas.simulate.assessment.runner;

import cn.iecas.simulate.assessment.entity.domain.IndexInfo;
import cn.iecas.simulate.assessment.entity.model.emun.ModelType;
import cn.iecas.simulate.assessment.service.IndexInfoService;
import cn.iecas.simulate.assessment.service.impl.RestTemplateApi;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;



@Order(2)
@Data
@Configuration
@Slf4j
@ConfigurationProperties(prefix = "assessment.init.zgdk")
public class InitIndexSystemInfoRunner<T> implements ApplicationRunner {

    private static final List<String> INIT_STATUS_INFO = new ArrayList<>();

    @Autowired
    private RestTemplateApi templateApi;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IndexInfoService indexInfoService;

    private List<String> tables;

    private List<String> modelsigns;

    private boolean enableforce;


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

        SqlRunner sqlRunner = new SqlRunner(dataSource.getConnection());
        for (String modelsign : modelsigns) {
            String sql = "SELECT count(*) FROM tb_index_info WHERE\tsign = " + "'" + modelsign + "' AND batch_no = 0";
            List<Map<String, Object>> maps = sqlRunner.selectAll(sql);
            if (maps.size() == 0) {
                INIT_STATUS_INFO.add("初始化失败，表 [tb_index_info] 不存在...");
                return;
            }
            Object count = maps.get(0).get("COUNT");
            if (count.toString().equalsIgnoreCase("0")) {
                this.buiderIndexInfoBySign(modelsign);
                INIT_STATUS_INFO.add("模型 " + modelsign + " 默认指标构建完成...");
            } else if (enableforce) {
                String deletesql = "DELETE FROM tb_index_info WHERE\tsign = " + "'" + modelsign + "' AND batch_no = 0";
                sqlRunner.delete(deletesql);
                this.buiderIndexInfoBySign(modelsign);
                INIT_STATUS_INFO.add("模型 " + modelsign + " 默认指标已存在，强制构建完成...");
            } else {
                INIT_STATUS_INFO.add("已存在标识为 " + modelsign + " 的默认指标...");
            }
        }

        for (String s : INIT_STATUS_INFO) {
            log.info(s);
        }
        log.info("跨域仿真初始化工作完成...");
    }


    /**
     * @Description 根据模型标识构建模型指标信息
     * @auther getao
     * @Date 2024/10/30 10:44
     * @Param [modelsign]
     * @Return void
     */
    private void buiderIndexInfoBySign(String modelsign) {
        if (modelsign.equalsIgnoreCase(ModelType.MFHFX.getSign())) {
            this.buiderIndexInfoFromMFHFX(modelsign);
        }

        if (modelsign.equalsIgnoreCase(ModelType.ZMDB.getSign())) {
            this.buiderIndexInfoFromMZMSLDB(modelsign);
        }
    }


    private void buiderIndexInfoFromMZMSLDB(String sign) {
        this.buiderCommonIndex(sign);

        IndexInfo zmdbAnalyse = IndexInfo.builder().indexName("中美力量实力对比评估能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(2).sign(sign).build();
        indexInfoService.insert(zmdbAnalyse);
        IndexInfo zmdbContibution = IndexInfo.builder().indexName("体系贡献率").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(2).sign(sign).build();
        indexInfoService.insert(zmdbContibution);

        IndexInfo zbgjAnalyse = IndexInfo.builder().indexName("中美实力评估指标构建能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zmdbAnalyse.getId()).sourceIndexId(0).level(3).sign(sign).build();
        indexInfoService.insert(zbgjAnalyse);
        IndexInfo zhdbAnalyse = IndexInfo.builder().indexName("中美力量实力综合对比评估能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zmdbAnalyse.getId()).sourceIndexId(0).level(3).sign(sign).build();
        indexInfoService.insert(zhdbAnalyse);
        IndexInfo jjdbAnalyse = IndexInfo.builder().indexName("中美经济力量对比分析能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zmdbAnalyse.getId()).sourceIndexId(0).level(3).sign(sign).build();
        indexInfoService.insert(jjdbAnalyse);
        IndexInfo jsllAnalyse = IndexInfo.builder().indexName("中美军事力量对比分析能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zmdbAnalyse.getId()).sourceIndexId(0).level(3).sign(sign).build();
        indexInfoService.insert(jsllAnalyse);
        IndexInfo kjslAnalyse = IndexInfo.builder().indexName("中美科技实力对比分析能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zmdbAnalyse.getId()).sourceIndexId(0).level(3).sign(sign).build();
        indexInfoService.insert(kjslAnalyse);

        IndexInfo zbgjType = IndexInfo.builder().indexName("指标构建类型").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zbgjAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(zbgjType);
        IndexInfo zbgjNum = IndexInfo.builder().indexName("指标构建数量").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zbgjAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(zbgjNum);
        IndexInfo zhdbfaType = IndexInfo.builder().indexName("综合对比维度类型").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zhdbAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(zhdbfaType);
        IndexInfo zhdbNum = IndexInfo.builder().indexName("综合对比指标数量").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zhdbAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(zhdbNum);
        IndexInfo jjdbType = IndexInfo.builder().indexName("经济力量对比维度类型").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(jjdbAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(jjdbType);
        IndexInfo jjdbNum = IndexInfo.builder().indexName("经济力量对比指标数量").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(jjdbAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(jjdbNum);
        IndexInfo jsllType = IndexInfo.builder().indexName("军事力量对比维度类型").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(jsllAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(jsllType);
        IndexInfo jsllNum = IndexInfo.builder().indexName("军事力量对比指标数量").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(jsllAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(jsllNum);
        IndexInfo kjslType = IndexInfo.builder().indexName("科技实力对比维度类型").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(kjslAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(kjslType);
        IndexInfo kjslNum = IndexInfo.builder().indexName("科技实力对比指标数量").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(kjslAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(kjslNum);
    }


    private void buiderIndexInfoFromMFHFX(String sign) {
        this.buiderCommonIndex(sign);

        IndexInfo fhgxAnalyse = IndexInfo.builder().indexName("府会关系分析能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(2).sign(sign).build();
        indexInfoService.insert(fhgxAnalyse);
        IndexInfo fhgxContibution = IndexInfo.builder().indexName("体系贡献率").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(2).sign(sign).build();
        indexInfoService.insert(fhgxContibution);

        IndexInfo zycwAnalyse = IndexInfo.builder().indexName("元首政要丑闻分析能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(fhgxAnalyse.getId()).sourceIndexId(0).level(3).sign(sign).build();
        indexInfoService.insert(zycwAnalyse);
        IndexInfo yyzcfaAnalyse = IndexInfo.builder().indexName("议员政策法案分析能力").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(fhgxAnalyse.getId()).sourceIndexId(0).level(3).sign(sign).build();
        indexInfoService.insert(yyzcfaAnalyse);

        IndexInfo zycwType = IndexInfo.builder().indexName("政要丑闻类型").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zycwAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(zycwType);
        IndexInfo zycwNum = IndexInfo.builder().indexName("政要丑闻数量").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(zycwAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(zycwNum);
        IndexInfo yyzcfaType = IndexInfo.builder().indexName("议员政策法案类型").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(yyzcfaAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(yyzcfaType);
        IndexInfo yyzcfaNum = IndexInfo.builder().indexName("议员政策法案数量").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(yyzcfaAnalyse.getId()).sourceIndexId(0).level(4).sign(sign).build();
        indexInfoService.insert(yyzcfaNum);
    }


    /**
     * 构建一级通用指标
     * @param sign
     */
    private void buiderCommonIndex(String sign) {
        IndexInfo usability = IndexInfo.builder().indexName("可用性").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(1).sign(sign).build();
        indexInfoService.insert(usability);
        IndexInfo realTime = IndexInfo.builder().indexName("数据实时性").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(1).sign(sign).build();
        indexInfoService.insert(realTime);
        IndexInfo conformity = IndexInfo.builder().indexName("接口符合性").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(1).sign(sign).build();
        indexInfoService.insert(conformity);
        IndexInfo normative = IndexInfo.builder().indexName("格式规范性").batchNo(0).creater("iecas").createTime(new Date())
                .parentIndexId(0).sourceIndexId(0).level(1).sign(sign).build();
        indexInfoService.insert(normative);
    }

}
