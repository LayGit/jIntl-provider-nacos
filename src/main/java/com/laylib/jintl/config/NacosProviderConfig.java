package com.laylib.jintl.config;

import com.laylib.jintl.annotation.ProviderType;
import com.laylib.jintl.loader.NacosSourceLoader;
import com.laylib.jintl.loader.SourceLoader;
import com.laylib.jintl.provider.DefaultMessageProvider;
import com.laylib.jintl.provider.MessageProvider;

import java.util.Properties;

/**
 * Nacos Provider Config
 *
 * @author Lay
 */
@ProviderType("nacos")
public class NacosProviderConfig extends BaseProviderConfig {

    private static final String DEFAULT_GROUP = "INTL";

    private static final Long DEFAULT_FETCH_TIMEOUT = 5000L;

    @Override
    public Class<? extends MessageProvider> getProviderClass() {
        return DefaultMessageProvider.class;
    }

    @Override
    public Class<? extends SourceLoader> getLoaderClass() {
        return NacosSourceLoader.class;
    }

    /**
     * nacos group
     */
    private String group;

    /**
     * fetch timeout
     */
    private Long fetchTimeout;

    /**
     * nacos config
     */
    private Properties config;

    public String getGroup() {
        if (group == null) {
            return DEFAULT_GROUP;
        }
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Long getFetchTimeout() {
        if (fetchTimeout == null) {
            return DEFAULT_FETCH_TIMEOUT;
        }
        return fetchTimeout;
    }

    public void setFetchTimeout(Long fetchTimeout) {
        this.fetchTimeout = fetchTimeout;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }
}
