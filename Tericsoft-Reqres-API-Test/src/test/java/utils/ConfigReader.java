package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;

    static {
        try {
            String path = "src/test/resources/config.properties";
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load config.properties file. Make sure it exists in src/test/resources.");
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}