package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

import java.util.List;

public interface ProcessDictionaryProvider<D extends ProcessDictionary> {
    D fetchDictionary(ProcessDefinitionConfig definition, String dictionaryId, String languageCode);
    D fetchDefaultDictionary(ProcessDefinitionConfig definition, String dictionaryId);
    List<D> fetchProcessDictionaries(ProcessDefinitionConfig definition);
    List<D> fetchAllDictionaries();
    List<D> fetchAllActiveDictionaries();
}
