package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

import java.util.HashMap;
import java.util.Map;

public class ProcessDictionaryRegistry {
    private static final EmptyDictionary EMPTY_DICTIONARY = new EmptyDictionary();

    private class RegisteredDictionaryHolder<T> {
        protected Map<String, T> dictionaries = new HashMap<String, T>();

        public T getDictionaryProvider(String providerId) {
            return dictionaries.get(providerId);
        }

        public void addDictionaryProvider(String providerId, T provider) {
            dictionaries.put(providerId, provider);
        }

        public void setDictionaries(Map<String, T> dictionaries) {
            this.dictionaries = dictionaries;
        }
    }

    protected RegisteredDictionaryHolder<ProcessDictionaryProvider> processDictionaryHolder =
            new RegisteredDictionaryHolder<ProcessDictionaryProvider>();
    protected RegisteredDictionaryHolder<GlobalDictionaryProvider> globalDictionaryHolder =
            new RegisteredDictionaryHolder<GlobalDictionaryProvider>();

    public ProcessDictionary getSpecificOrDefaultOrGlobalDictionary(ProcessDefinitionConfig definition,
                                                                    String providerId, String dictionaryId, String languageCode) {
        ProcessDictionary dictionary = getSpecificOrDefaultProcessDictionary(definition, providerId, dictionaryId, languageCode);
            if (dictionary == null) {
            dictionary = getSpecificOrDefaultGlobalDictionary(providerId, dictionaryId, languageCode);
            }
        return dictionary;
        }

    public ProcessDictionary getSpecificOrDefaultProcessDictionary(ProcessDefinitionConfig definition,
                                                                   String providerId, String dictionaryId, String languageCode) {
        ProcessDictionary dictionary = getSpecificProcessDictionary(definition, providerId, dictionaryId, languageCode);
        if (dictionary == null) {
            dictionary = getDefaultProcessDictionary(definition, providerId, dictionaryId);
        }
        return dictionary;
    }

    public ProcessDictionary getSpecificProcessDictionary(ProcessDefinitionConfig definition, String providerId, String dictionaryId,
                                                          String languageCode) {
        ProcessDictionaryProvider provider = processDictionaryHolder.getDictionaryProvider(providerId);
        return provider != null ? provider.fetchProcessDictionary(definition, dictionaryId, languageCode) : null;
    }

    public ProcessDictionary getDefaultProcessDictionary(ProcessDefinitionConfig definition, String providerId, String dictionaryId) {
        ProcessDictionaryProvider provider = processDictionaryHolder.getDictionaryProvider(providerId);
        return provider != null ? provider.fetchDefaultProcessDictionary(definition, dictionaryId) : null;
    }

    public ProcessDictionaryProvider getProcessDictionaryProvider(String providerId) {
        return processDictionaryHolder.getDictionaryProvider(providerId);
    }

    public void addProcessDictionaryProvider(String providerId, ProcessDictionaryProvider provider) {
        processDictionaryHolder.addDictionaryProvider(providerId, provider);
    }

    public ProcessDictionary getSpecificOrDefaultGlobalDictionary(String providerId, String dictionaryId, String languageCode) {
        ProcessDictionary dictionary = getSpecificGlobalDictionary(providerId, dictionaryId, languageCode);
        if (dictionary == null) {
            dictionary = getDefaultGlobalDictionary(providerId, dictionaryId);
        }
        return dictionary;
    }

    public ProcessDictionary getSpecificGlobalDictionary(String providerId, String dictionaryId, String languageCode) {
        GlobalDictionaryProvider provider = globalDictionaryHolder.getDictionaryProvider(providerId);
        return provider != null ? provider.fetchGlobalDictionary(dictionaryId, languageCode) : null;
    }

    public ProcessDictionary getDefaultGlobalDictionary(String providerId, String dictionaryId) {
        GlobalDictionaryProvider provider = globalDictionaryHolder.getDictionaryProvider(providerId);
        return provider != null ? provider.fetchDefaultGlobalDictionary(dictionaryId) : null;
    }

    public GlobalDictionaryProvider getGlobalDictionaryProvider(String providerId) {
        return globalDictionaryHolder.getDictionaryProvider(providerId);
    }

    public void addGlobalDictionaryProvider(String providerId, GlobalDictionaryProvider provider) {
        globalDictionaryHolder.addDictionaryProvider(providerId, provider);
    }

    public ProcessDictionary getEmptyDictionary() {
        return EMPTY_DICTIONARY;
    }
}
