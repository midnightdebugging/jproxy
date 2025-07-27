package org.pierce;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class DataBase {
    private static SqlSessionFactory sqlSessionFactory;

    private static final Logger log = LoggerFactory.getLogger(DataBase.class);


    public static void initialize() {
        String environmentId = JproxyProperties.getProperty("mybatis.env", "development"); // 默认使用开发环境
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, environmentId, JproxyProperties.getProperties());

        } catch (IOException e) {
            log.warn("IOException", e);
            throw new RuntimeException(e);
        }
    }

    public static void initialize(String... sqlClassPaths) throws ClassNotFoundException {
        //String jdbcFileDir = JproxyProperties.getProperty("jdbc.file-dir");
        String jdbcFileName = JproxyProperties.getProperty("jdbc.file-name");
        //File file0 = new File(jdbcFileDir);
        File file1 = new File(jdbcFileName);
        /*if (!file0.exists()) {
            if (!file0.mkdir()) {
                log.info("mkdir {} error {}", jdbcFileDir, "!file0.mkdir()");
                System.exit(1);
                return;
            }
        }*/

        if (!file1.exists()) {
            String jdbcUrl = JproxyProperties.getProperty("jdbc.url");
            String username = JproxyProperties.getProperty("jdbc.username");
            String password = JproxyProperties.getProperty("jdbc.password");
            String clazz = JproxyProperties.getProperty("jdbc.driver");
            Class.forName(clazz);
            try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                if (sqlClassPaths != null) {
                    for (String sqlClassPath : sqlClassPaths) {
                        executeSqlFile(conn, sqlClassPath);
                    }
                }

            } catch (Exception e) {
                log.info("Exception", e);
            }
        }


        initialize();
    }


    public static void executeSqlFile(Connection conn, String classpath) throws Exception {

        // 创建 ScriptRunner 实例
        ScriptRunner runner = new ScriptRunner(conn);

        // 配置选项
        runner.setAutoCommit(false);             // 关闭自动提交
        runner.setStopOnError(true);             // 遇到错误时停止
        runner.setLogWriter(null);               // 禁用日志输出到控制台
        runner.setErrorLogWriter(null);         // 禁用错误日志输出
        runner.setSendFullScript(false);         // 逐条执行（非整条脚本）
        runner.setDelimiter(";");               // 设置语句分隔符
        runner.setFullLineDelimiter(false);      // 不使用整行作为分隔符

        try (InputStream is = DataBase.class.getResourceAsStream(classpath); Reader reader = new InputStreamReader(is)) {
            log.info("Executing SQL file: {}", classpath);
            runner.runScript(reader);

            conn.commit();  // 手动提交事务
            log.info("SQL file executed successfully!");
        } catch (Exception e) {
            conn.rollback();  // 出错时回滚
            throw e;
        }
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }


}
