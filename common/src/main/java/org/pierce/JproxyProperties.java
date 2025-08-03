package org.pierce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JproxyProperties {

    public final static Pattern pattern = Pattern.compile("\\$\\{([^${}]+)}");

    public static Properties properties = new Properties();

    private static final Logger log = LoggerFactory.getLogger(JproxyProperties.class);

    public static void loadPropertiesByClassPath(String path) {
        loadPropertiesByClassPath(path, false);
    }

    public static void loadPropertiesByClassPath(String path, boolean necessary) {
        log.info("loadProperties:{}", path);
        try (InputStream inputStream = UtilTools.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                log.error("inputStream ==null");
                if (necessary) {
                    throw new RuntimeException("inputStream ==null");
                }
            }
            if (inputStream != null) {
                properties.load(inputStream);
            }

        } catch (IOException e) {
            log.error("IOException", e);
            if (necessary) {
                throw new RuntimeException(e);
            }
        }

    }

    final static Pattern rootPathTest = Pattern.compile("^/|^[a-zA-Z]:\\\\");

    public static void loadPropertiesByFilePath(String path) {
        loadPropertiesByFilePath(path, false);

    }

    public static void loadPropertiesByFilePath(String path, boolean necessary) {
        String fullPath = path;
        if (!rootPathTest.matcher(path).find()) {
            fullPath = evaluate("${user.dir}/" + path);

        }

        log.info(fullPath);
        try (InputStream inputStream = Files.newInputStream(Paths.get(fullPath))) {
            properties.load(inputStream);

        } catch (IOException e) {
            log.error("IOException", e);
            if (necessary) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void initialize() {

        loadPropertiesByClassPath("/application-common.properties",true);
        loadPropertiesByClassPath("/application.properties",true);

        String env = System.getProperty("env");
        if (env == null || env.isEmpty()) {
            env = properties.getProperty("env");
        }
        if (env == null || env.isEmpty()) {
            env = "dev";
            properties.setProperty("env", "dev");
        }

        loadPropertiesByClassPath(evaluate("/application-${env}.properties"));

        properties.putAll(System.getProperties());

        loadPropertiesByFilePath(getProperty("tls.properties"));




        properties.setProperty("jdbc.file-dir", JproxyProperties.getProperty("jdbc.file-dir"));
        properties.setProperty("jdbc.file-name", JproxyProperties.getProperty("jdbc.file-name"));
        properties.setProperty("jdbc.url", JproxyProperties.getProperty("jdbc.url"));

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            sb.append(String.format("%s=%s\n", String.valueOf(entry.getKey()), String.valueOf(entry.getValue())));
        }
        log.info(sb.toString());


    }

    static {
        initialize();
    }


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
        return "true".equals(strVal);
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        JproxyProperties.properties = properties;
    }

    public static void reloadTlsProperties() {
        loadPropertiesByFilePath(getProperty("tls.properties"));
    }
}
