package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProcessDictionaryRegistry {
    private static final EmptyDictionary EMPTY_DICTIONARY = new EmptyDictionary();

    private final Map<String, GlobalDictionaryProvider> providersByProviderId = new LinkedHashMap<String, GlobalDictionaryProvider>();

    public ProcessDictionary getDictionary(String dictionaryId) {
		for (GlobalDictionaryProvider provider : providersByProviderId.values()) {
			ProcessDictionary dictionary = provider.fetchDictionary(dictionaryId);
			if (dictionary != null) {
				return dictionary;
			}
		}
		return null;
    }

    public GlobalDictionaryProvider getDictionaryProvider(String providerId) {
        return providersByProviderId.get(providerId);
    }

    public void addDictionaryProvider(String providerId, GlobalDictionaryProvider provider) {
		providersByProviderId.put(providerId, provider);
    }

    public ProcessDictionary getEmptyDictionary() {
        return EMPTY_DICTIONARY;
    }
}
