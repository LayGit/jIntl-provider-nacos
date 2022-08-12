package com.laylib.jintl.test.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.laylib.jintl.IntlSource;
import com.laylib.jintl.config.NacosProviderConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Properties;

/**
 * Nacos Provider Test Cases
 *
 * @author Lay
 */
public class TestNacos {

    @BeforeEach
    public void init() {
        System.setProperty("nacos.logging.default.config.enabled", "false");
    }

    @Test
    public void test() {
        NacosProviderConfig config = new NacosProviderConfig();
        config.setGroup("INTL");
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, "127.0.0.1:8848");
        config.setConfig(properties);

        IntlSource intlSource = new IntlSource(config);
        String msg = intlSource.getMessage("http.internalServerError", Locale.ENGLISH);
        Assertions.assertEquals("Internal Server Error", msg);
    }
}
