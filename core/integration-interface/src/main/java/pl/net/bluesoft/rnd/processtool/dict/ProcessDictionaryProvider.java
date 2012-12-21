package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

import java.util.List;

public interface ProcessDictionaryProvider<D extends ProcessDictionary> {
    D fetchProcessDictionary(ProcessDefinitionConfig definition, String dictionaryId, String languageCode);
    D fetchDefaultProcessDictionary(ProcessDefinitionConfig definition, String dictionaryId);
    List<D> fetchProcessDictionaries(ProcessDefinitionConfig definition);
    List<D> fetchAllProcessDictionaries();
    List<D> fetchAllActiveProcessDictionaries();
}
