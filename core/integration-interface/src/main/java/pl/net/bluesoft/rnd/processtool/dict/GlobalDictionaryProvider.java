package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

import java.util.List;

public interface GlobalDictionaryProvider<D extends ProcessDictionary> {
    D fetchGlobalDictionary(String dictionaryId, String languageCode);
    D fetchDefaultGlobalDictionary(String dictionaryId);
    List<D> fetchAllGlobalDictionaries();
}
