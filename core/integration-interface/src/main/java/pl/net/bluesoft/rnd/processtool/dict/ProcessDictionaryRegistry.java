package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

import java.util.HashMap;
import java.util.Map;

public class ProcessDictionaryRegistry {
    protected Map<String, ProcessDictionaryProvider> dictionaries = new HashMap<String, ProcessDictionaryProvider>();

    public ProcessDictionary getSpecificOrDefaultDictionary(ProcessDefinitionConfig definition, String providerId, String dictionaryId, String languageCode) {
        ProcessDictionaryProvider processDictionaryProvider = dictionaries.get(providerId);
        ProcessDictionary dictionary = null;
        if (processDictionaryProvider != null) {
            dictionary = processDictionaryProvider.fetchDictionary(definition, dictionaryId, languageCode);
            if (dictionary == null) {
                dictionary = processDictionaryProvider.fetchDefaultDictionary(definition, dictionaryId);
            }
        }
        return dictionary;
    }

    public ProcessDictionary getSpecificDictionary(ProcessDefinitionConfig definition, String providerId, String dictionaryId, String languageCode) {
        ProcessDictionaryProvider processDictionaryProvider = dictionaries.get(providerId);
        return processDictionaryProvider != null ? processDictionaryProvider.fetchDictionary(definition, dictionaryId, languageCode) : null;
    }

    public ProcessDictionary getDefaultDictionary(ProcessDefinitionConfig definition, String providerId, String dictionaryId) {
        ProcessDictionaryProvider processDictionaryProvider = dictionaries.get(providerId);
        return processDictionaryProvider != null ? processDictionaryProvider.fetchDefaultDictionary(definition, dictionaryId) : null;
    }

    public ProcessDictionaryProvider getDictionaryProvider(String providerId) {
        return dictionaries.get(providerId);
    }

    public void addDictionaryProvider(String providerId, ProcessDictionaryProvider provider) {
        dictionaries.put(providerId, provider);
    }

    public void setDictionaries(Map<String, ProcessDictionaryProvider> dictionaries) {
        this.dictionaries = dictionaries;
    }
}
