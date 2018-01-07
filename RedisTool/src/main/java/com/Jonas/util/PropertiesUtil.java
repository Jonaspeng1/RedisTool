package com.Jonas.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * User: Janon
 * Date: 14-6-13 下午5:05
 */
public class PropertiesUtil {
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private PropertiesUtil() {
    }

    public static Properties loadProperties(String file) {
        Properties properties = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            properties.load(fis);
        } catch (Throwable ignore) {
            if (ignore instanceof FileNotFoundException) {
                logger.error(ignore.getMessage());
                properties = null;
            } else {
                logger.error(ignore.getMessage(), ignore);
            }
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return properties;
    }
}
