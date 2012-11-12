package pl.net.bluesoft.rnd.pt.dict.global.i18n;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemValue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NProvider;
import pl.net.bluesoft.util.lang.ExpiringCache;
import pl.net.bluesoft.util.lang.ExpiringCache.NewValueCallback;
import pl.net.bluesoft.util.lang.Strings;

import java.util.Locale;
import java.util.Properties;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class GlobalDictionaryI18NProvider implements I18NProvider {
    private ProcessToolRegistry registry;
    private String dictionaryId;

    private static final String EMPTY_LANGUAGE_CODE = "";
    private static final String CONFIG_CACHE_REFRESH_INTERVAL = "cache.refresh.interval";
    private static final long DEFAULT_CACHE_REFRESH_INTERVAL = 360 * 1000;
    private ExpiringCache<String, ProcessDictionary> cache;

    public GlobalDictionaryI18NProvider(String dictionaryId, ProcessToolRegistry registry, Properties properties) {
        this.registry = registry;
        this.dictionaryId = dictionaryId;
        String interval = properties.getProperty(CONFIG_CACHE_REFRESH_INTERVAL);
        this.cache = new ExpiringCache<String, ProcessDictionary>(Strings.hasText(interval) ? Integer.parseInt(interval) * 1000L : DEFAULT_CACHE_REFRESH_INTERVAL);
    }

    @Override
    public boolean hasFullyLocalizedMessage(String key, Locale locale) {
        ProcessDictionary dictionary = fetchSpecificDictionary(fullyLocalizedLanguageCode(locale));
        return dictionary != null && dictionary.containsKey(key);
    }

    @Override
    public boolean hasLocalizedMessage(String key, Locale locale) {
        ProcessDictionary dictionary = fetchSpecificDictionary(localizedLanguageCode(locale));
        return dictionary != null && dictionary.containsKey(key);
    }

    @Override
    public String getMessage(String key, Locale locale) {
        ProcessDictionary dictionary = fetchSpecificDictionary(fullyLocalizedLanguageCode(locale));
        if (dictionary == null) {
            dictionary = fetchSpecificDictionary(localizedLanguageCode(locale));
        }
        if (dictionary == null) {
            dictionary = fetchDefaultDictionary();
        }
        if (dictionary != null && dictionary.containsKey(key)) {
            ProcessDictionaryItem<String, String> item = dictionary.lookup(key);
            ProcessDictionaryItemValue<String> value = item.getValueForCurrentDate();
            if (value != null) {
                return value.getValue();
            }
        }
        return null;
    }

    private String fullyLocalizedLanguageCode(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    private String localizedLanguageCode(Locale locale) {
        return locale.getLanguage();
    }

    private ProcessDictionary fetchSpecificDictionary(final String languageCode) {
        return cache.get(languageCode, new NewValueCallback<String, ProcessDictionary>() {
            @Override
            public ProcessDictionary getNewValue(String key) {
                return registry.withProcessToolContext(new ReturningProcessToolContextCallback<ProcessDictionary>() {
                    @Override
                    public ProcessDictionary processWithContext(ProcessToolContext ctx) {
                        ProcessDictionaryRegistry dictionaryRegistry = ctx.getProcessDictionaryRegistry();
                        return dictionaryRegistry.getSpecificGlobalDictionary("db", dictionaryId, languageCode);
                    }
                });
            }
        });
    }

    private ProcessDictionary fetchDefaultDictionary() {
        return cache.get(EMPTY_LANGUAGE_CODE, new NewValueCallback<String, ProcessDictionary>() {
            @Override
            public ProcessDictionary getNewValue(String key) {
                return registry.withProcessToolContext(new ReturningProcessToolContextCallback<ProcessDictionary>() {
                    @Override
                    public ProcessDictionary processWithContext(ProcessToolContext ctx) {
                        ProcessDictionaryRegistry dictionaryRegistry = ctx.getProcessDictionaryRegistry();
                        return dictionaryRegistry.getDefaultGlobalDictionary("db", dictionaryId);
                    }
                });
            }
        });
    }
}
