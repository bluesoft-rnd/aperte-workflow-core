package pl.net.bluesoft.rnd.processtool;

import org.hibernate.classic.Session;
import org.junit.Before;
import org.junit.Test;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryLoader;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryProvider;
import pl.net.bluesoft.rnd.processtool.dict.xml.ProcessDictionaries;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistryImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DictionaryTest {
    private ProcessToolRegistry registry;

    @Before
    public void setup() {
        registry = new ProcessToolRegistryImpl();
    }

    @Test
    public void testDBDictionary() {
        Session sess = null;
        try {
            sess = registry.getSessionFactory().openSession();
            ProcessDictionaryProvider provider = (ProcessDictionaryProvider) registry.getProcessDictionaryDAO(sess);
            List<ProcessDictionary> dictionaries = provider.fetchAllDictionaries();
            Set<String> dictNames = new HashSet<String>();
            ProcessDefinitionConfig config = null;
            String dictName = null;
            for (ProcessDictionary dict : dictionaries) {
                dictNames.add(dict.getDictionaryId());
                config = ((ProcessDBDictionary) dict).getProcessDefinition();
                dictName = dict.getDictionaryId();
            }
            System.out.println("dictNames = " + dictNames);

            ProcessDictionary dict2 = provider.fetchDefaultDictionary(config, dictName);
            System.out.println(dict2.getLanguageCode());
        }
        catch (Exception e) {
            if (sess != null) {
                sess.close();
            }
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadDict() {
        ProcessDictionaries dictionaries = (ProcessDictionaries) DictionaryLoader.getInstance().unmarshall(ClassLoader.getSystemResourceAsStream("test-dict.xml"));
        System.out.println(dictionaries.getProcessBpmDefinitionKey());
    }
}
