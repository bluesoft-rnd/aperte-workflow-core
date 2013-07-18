package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

import java.util.List;

public interface GlobalDictionaryProvider<D extends ProcessDictionary> {
    D fetchDictionary(String dictionaryId);
    List<D> fetchAllDictionaries();
}
