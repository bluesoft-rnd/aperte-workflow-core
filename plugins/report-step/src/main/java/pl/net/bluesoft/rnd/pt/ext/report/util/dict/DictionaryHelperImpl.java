package pl.net.bluesoft.rnd.pt.ext.report.util.dict;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;

import java.util.HashMap;
import java.util.Map;

/**
 * DB based implementation of DictionaryHelper.
 */
public class DictionaryHelperImpl implements DictionaryHelper {
    private static final String BLANK = "";
    protected ProcessDictionaryRegistry processDictionaryRegistry;
    protected ProcessDefinitionConfig processDefinitionConfig;
    protected Map<String, Map<String, String>> dictionaryMap;
    
    @Override
    public synchronized String getDictionaryValue(String dictionaryName, String languageCode, String key) {
        if (dictionaryName != null && languageCode != null && key != null) {
            if (!dictionaryMap.containsKey(dictionaryKey(languageCode, dictionaryName)))
                addDictionary(languageCode, dictionaryName);
            if (dictionaryMap.containsKey(dictionaryKey(languageCode, dictionaryName))) {
                Map<String, String> dictionary = dictionaryMap.get(dictionaryKey(languageCode, dictionaryName));
                if (dictionary.containsKey(key))
                    return dictionary.get(key);
            }
        }
        return BLANK;
    }

    private void addDictionary(String languageCode, String dictionaryName) {
        ProcessDictionary dict = processDictionaryRegistry.getSpecificOrDefaultOrGlobalDictionary(
                processDefinitionConfig, "db", dictionaryName, languageCode);
        if (dict != null) {
            Map<String, String> dictionary = new HashMap<String, String>();
            for (Object item : dict.items()) {
                ProcessDictionaryItem pdItem = (ProcessDictionaryItem) item;
                dictionary.put(pdItem.getKey().toString(), pdItem.getValueForCurrentDate().toString());
            }
            dictionaryMap.put(dictionaryKey(languageCode, dictionaryName), dictionary);
        }
    }

    public DictionaryHelperImpl(ProcessInstance processInstance) {
        init(processInstance);
    }
    
    private void init(ProcessInstance processInstance) {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        processDictionaryRegistry = ctx.getProcessDictionaryRegistry();
        processDefinitionConfig = processInstance.getDefinition();
        dictionaryMap = new HashMap<String, Map<String, String>>();
    }

    protected String dictionaryKey(String languageCode, String dictionaryName) {
        return languageCode.concat("_").concat(dictionaryName);
    }
}
