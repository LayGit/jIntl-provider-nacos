package com.laylib.jintl.loader;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.laylib.jintl.config.NacosProviderConfig;
import com.laylib.jintl.entity.SourceIndex;
import com.laylib.jintl.entity.SourceProperties;
import com.laylib.jintl.monitor.NacosSourceMonitor;
import com.laylib.jintl.monitor.SourceMonitor;
import com.laylib.jintl.parser.SourceParser;
import com.laylib.jintl.parser.SourceParserDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 * Nacos Source Loader
 *
 * @author Lay
 */
public class NacosSourceLoader extends AbstractSourceLoader<NacosProviderConfig> {

    private static final Logger logger = LoggerFactory.getLogger(NacosSourceLoader.class);

    private final ConfigService configService;

    public NacosSourceLoader(NacosProviderConfig config) {
        super(config);

        try {
            this.configService = NacosFactory.createConfigService(config.getConfig());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected SourceMonitor createMonitor() {
        return new NacosSourceMonitor(config, configService, this::onIndexChange, this::onSourceChange);
    }

    @Override
    public SourceIndex loadIndex() {
        try {
            String index = configService.getConfig(config.getIndex(), config.getGroup(), config.getFetchTimeout());
            return loadIndex(index);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    private SourceIndex loadIndex(String index) {
        if (index == null) {
            return null;
        }

        Yaml yaml = new Yaml();
        Map<String, List<String>> props = yaml.load(index);
        return resolveIndex(props);
    }

    @Override
    public List<SourceProperties> loadSources(SourceIndex index) {
        List<SourceProperties> sourceProperties = new ArrayList<>();
        for (SourceIndex.IndexItem indexItem : index.getItems()) {
            for (Locale locale : indexItem.getLocales()) {
                sourceProperties.add(loadSource(indexItem.getTag(), locale));
            }
        }
        return sourceProperties;
    }

    @Override
    public SourceProperties loadSource(String tag, Locale locale) {
        try {
            String sourcePath = getSourcePath(tag, locale);
            String source = configService.getConfig(sourcePath, config.getGroup(), config.getFetchTimeout());
            SourceParser sourceParser = SourceParserDetector.detect(sourcePath, config.getCharset());
            Properties props = sourceParser.parse(source);
            return new SourceProperties(tag, locale, props);
        } catch (NacosException e) {
            logger.warn("Source of Tag: {} and Locale: {} load failed {}", tag, locale.toLanguageTag(), e);
        }

        return null;
    }

    @Override
    public String getSourcePath(String tag, Locale locale) {
        return sourceNameFormatter.format(tag, locale);
    }

    protected void onIndexChange(String index) {
        SourceIndex sourceIndex = loadIndex(index);
        this.indexChangedListener.onChange(sourceIndex);
    }

    protected void onSourceChange(String tag, Locale locale, String source) {
        SourceParser sourceParser = SourceParserDetector.get(config.getSourceFileExtension(), config.getCharset());
        Properties props = sourceParser.parse(source);
        this.sourceChangedListener.onChange(Collections.singletonList(new SourceProperties(tag, locale, props)));
    }
}
