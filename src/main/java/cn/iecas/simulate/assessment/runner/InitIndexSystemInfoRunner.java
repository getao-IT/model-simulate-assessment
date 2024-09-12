package cn.iecas.simulate.assessment.runner;

import cn.aircas.utils.file.FileUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Data
@Configuration
@Slf4j
@ConfigurationProperties(prefix = "value.init")
public class InitIndexSystemInfoRunner<T> implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    private List<String> tables;

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

        String initPath = FileUtils.getStringPath(System.getProperty("user.dir"), "init");
        InputStream inputStream  = null;
        InputStreamReader reader = null;
        SqlRunner sqlRunner = new SqlRunner(dataSource.getConnection());
        String tbQuerySql = "SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename LIKE 'tb_%'";
        List<Map<String, Object>> maps = sqlRunner.selectAll(tbQuerySql);
        List<String> tablename = maps.stream().map(e -> e.get("TABLENAME")).map(String::valueOf).collect(Collectors.toList());
        for (String table : tables) {
            String sqlPath = FileUtils.getStringPath(initPath, table) + ".sql";
            if (tablename.contains(table)) {
                if (!enableforce) {
                    log.info("已存在该表 {}", table);
                    continue;
                }
                inputStream = new FileInputStream(sqlPath);
                reader = new InputStreamReader(inputStream, "utf-8");
                runner.runScript(reader);
                log.info("存在该表 {}，强制初始化已开启，初始化成功...", table);
            } else {
                inputStream = new FileInputStream(sqlPath);
                reader = new InputStreamReader(inputStream, "utf-8");
                runner.runScript(reader);
                log.info("表 {} 初始化成功...", table);
            }
        }

        if (reader != null) {
            reader.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }

        log.info("跨域仿真初始化工作完成...");
   }
}
