package pl.net.bluesoft.rnd.pt.dict.global;

import pl.net.bluesoft.rnd.pt.dict.global.i18n.GlobalDictionaryI18NProvider;
import pl.net.bluesoft.rnd.pt.dict.global.xml.Dictionary;
import pl.net.bluesoft.rnd.pt.dict.global.xml.ProcessDictionaries;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author: amichalak@bluesoft.net.pl
 */
public class GlobalDictionariesActivator extends AbstractPluginActivator {
    private static final String PROVIDER_ID = GlobalDictionariesActivator.class.getName();

    private List<String> dictionaryNames;

    @Override
    protected void init() throws Exception {
        this.dictionaryNames = new ArrayList<String>();
        InputStream is = loadResourceAsStream("global-dictionaries.xml");
        if (is == null) {
            throw new IllegalArgumentException("Global dictionaries XML not found!");
        }
        Properties properties = loadProperties("plugin.properties");
        ProcessDictionaries dictionaries = (ProcessDictionaries) DictionaryLoader.getInstance().unmarshall(is);
        for (Dictionary dictionary : dictionaries.getDictionaries()) {
            String dictionaryId = dictionary.getDictionaryId();
            dictionaryNames.add(dictionaryId);
            registry.registerI18NProvider(new GlobalDictionaryI18NProvider(dictionaryId, registry, properties), getDictionaryProviderId(dictionaryId));
        }
    }

    @Override
    protected void destroy() throws Exception {
        for (String name : dictionaryNames) {
            registry.unregisterI18NProvider(getDictionaryProviderId(name));
        }
    }

    private String getDictionaryProviderId(String dictionaryId) {
        return PROVIDER_ID + "_" + dictionaryId;
    }
}
