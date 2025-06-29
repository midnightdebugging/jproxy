package org.pierce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JproxyProperties {

    public final static Pattern pattern = Pattern.compile("\\$\\{([^${}]+)}");

    public static Properties properties = new Properties();

    private static final Logger log = LoggerFactory.getLogger(JproxyProperties.class);


    static {


        try (InputStream is = UtilTools.class.getResourceAsStream("/application-common.properties")) {
            log.info("load:/application-common.properties");
            if (is != null) {
                properties.load(is);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try (InputStream is = UtilTools.class.getResourceAsStream("/application.properties")) {
            log.info("load:/application.properties");
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String env = System.getProperty("env");
        if (env == null || env.isEmpty()) {
            env = properties.getProperty("env");
        }
        if (env == null || env.isEmpty()) {
            env = "dev";
            properties.setProperty("env", "dev");
        }

        String envProp = String.format("/application-%s.properties", env);

        try (InputStream is = UtilTools.class.getResourceAsStream(envProp)) {
            log.info("load:{}", envProp);
            if (is != null) {
                properties.load(is);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        properties.putAll(System.getProperties());

        try (FileInputStream fileInputStream = new FileInputStream(JproxyProperties.getProperty("tls.properties"))) {
            log.info("load:{}", JproxyProperties.getProperty("tls.properties"));
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.warn(JproxyProperties.getProperty("tls.properties") + "no exists");
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            sb.append(String.format("%s=%s\n", String.valueOf(entry.getKey()), String.valueOf(entry.getValue())));
        }
        log.info(sb.toString());

        properties.setProperty("jdbc.file-dir", JproxyProperties.getProperty("jdbc.file-dir"));
        properties.setProperty("jdbc.file-name", JproxyProperties.getProperty("jdbc.file-name"));
        properties.setProperty("jdbc.url", JproxyProperties.getProperty("jdbc.url"));
        /*jdbc.file-dir=${user.home}/.jproxy
        jdbc.file-name=${jdbc.file-dir}/jproxy-${env}.sqlite3
        jdbc.url=jdbc:sqlite:${jdbc.file-name}?journal_mode=WAL&busy_timeout=5000*/
    }

    /*public static String currentTime() {
        return new Timestamp(System.currentTimeMillis()).toString();
    }*/

    public static String getProperty(String key) {
        if (properties.containsKey(key)) {
            Object obj = properties.get(key);
            if (obj != null) {
                return evaluate(obj.toString());
            }
            return "";
        }
        return "";
    }

    public static String getProperty(String key, String def) {
        if (properties.containsKey(key)) {
            Object obj = properties.get(key);
            if (obj != null) {
                return evaluate(obj.toString());
            }
            return "";
        }
        return def;
    }

    public static String evaluate(String testStr) {

        while (true) {

            Matcher matcher = pattern.matcher(testStr);
            if (!matcher.find()) {
                break;
            }
            int start = matcher.start();
            int end = matcher.end();

            String replace = JproxyProperties.getProperty(matcher.group(1), "");
            testStr = testStr.substring(0, start) + replace + testStr.substring(end);
        }
        return testStr;
    }

    public static boolean booleanVal(String key) {
        String strVal = getProperty(key, "false");
        //System.out.printf("%s==>%s\n", key, strVal);
        return "true".equals(strVal);
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        JproxyProperties.properties = properties;
    }

    public static void reloadTlsProperties() {

        try (FileInputStream fileInputStream = new FileInputStream(JproxyProperties.getProperty("tls.properties"))) {
            properties.load(fileInputStream);
            StringBuilder sb = new StringBuilder();
            sb.append(JproxyProperties.getProperty("tls.properties"));
            sb.append(" done\n");
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                sb.append(String.format("%s=%s\n", String.valueOf(entry.getKey()), String.valueOf(entry.getValue())));
            }
            log.info(sb.toString());

        } catch (IOException e) {
            log.warn(JproxyProperties.getProperty("tls.properties") + " no exists");
        }
    }
}
