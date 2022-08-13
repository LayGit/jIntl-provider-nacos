package com.laylib.jintl.monitor;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.laylib.jintl.config.NacosProviderConfig;
import com.laylib.jintl.entity.SourceIndex;
import com.laylib.jintl.formatter.SourceNameFormatter;
import com.laylib.jintl.formatter.SourceNameFormatterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * Nacos Source Monitor
 *
 * @author Lay
 */
public class NacosSourceMonitor extends AbstractSourceMonitor<NacosProviderConfig> {

    private static final Logger logger = LoggerFactory.getLogger(NacosSourceMonitor.class);

    private final ConfigService configService;

    private volatile boolean indexWatching;

    private volatile boolean sourceWatching;

    private final SourceChangedListener sourceChangedListener;

    private final Listener indexListener;

    private final ConcurrentMap<String, Listener> configListenerMap;

    public NacosSourceMonitor(NacosProviderConfig config, ConfigService configService, IndexChangedListener indexChangedListener, SourceChangedListener sourceChangedListener) {
        super(config);
        this.configService = configService;
        this.sourceChangedListener = sourceChangedListener;
        this.configListenerMap = new ConcurrentHashMap<>();
        this.indexListener = new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                indexChangedListener.onChange(configInfo);
            }
        };
    }

    @Override
    public void startIndexMonitor() {
        if (indexWatching) {
            return;
        }

        try {
            configService.addListener(config.getIndex(), config.getGroup(), indexListener);
            indexWatching = true;
        } catch (NacosException e) {
            logger.error("Index Monitor started failed:", e);
        }
    }

    @Override
    public void startSourceMonitor() {
        if (sourceWatching) {
            return;
        }

        addSourceListeners();
        sourceWatching = true;
    }

    @Override
    public void stopIndexMonitor() {
        if (indexWatching) {
            configService.removeListener(config.getIndex(), config.getGroup(), indexListener);
            indexWatching = false;
        }
    }

    @Override
    public void stopSourceMonitor() {
        if (sourceWatching) {
            clearSourceListeners();
            sourceWatching = false;
        }
    }

    @Override
    public void watchSourcesWithIndex(SourceIndex sourceIndex) {
        Map<String, Listener> map = new HashMap<>();
        SourceNameFormatter sourceNameFormatter = SourceNameFormatterFactory.build(config.getSourceNameFormatterClass(), config.getSourceFileExtension());

        for (SourceIndex.IndexItem item : sourceIndex.getItems()) {
            if (sourceIndex.getItems().isEmpty()) {
                return;
            }

            String tag = item.getTag();
            for (Locale locale : item.getLocales()) {
                Listener listener = new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        sourceChangedListener.onChange(tag, locale, configInfo);
                    }
                };
                map.put(sourceNameFormatter.format(tag, locale), listener);
            }
        }

        this.clearSourceListeners();

        synchronized (this.configListenerMap) {
            this.configListenerMap.clear();
            this.configListenerMap.putAll(map);
        }

        this.addSourceListeners();
    }

    private void addSourceListeners() {
        for (Map.Entry<String, Listener> entry : this.configListenerMap.entrySet()) {
            try {
                configService.addListener(entry.getKey(), config.getGroup(), entry.getValue());
            } catch (NacosException e) {
                logger.warn("source {} watch failed: {}", entry.getKey(), e);
            }
        }
    }

    private void clearSourceListeners() {
        for (Map.Entry<String, Listener> entry : this.configListenerMap.entrySet()) {
            configService.removeListener(entry.getKey(), config.getGroup(), entry.getValue());
        }
    }

    public interface IndexChangedListener {
        void onChange(String index);
    }

    public interface SourceChangedListener {
        void onChange(String tag, Locale locale, String source);
    }
}
