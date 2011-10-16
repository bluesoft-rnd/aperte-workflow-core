package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;

import java.util.List;

public interface ProcessDictionaryDAO {
    ProcessDBDictionary fetchDictionary(ProcessDefinitionConfig definition, String dictionaryId, String languageCode);
    List<ProcessDBDictionary> fetchProcessDictionaries(ProcessDefinitionConfig definition);
    List<ProcessDBDictionary> fetchAllDictionaries();
    void createOrUpdateProcessDictionary(ProcessDefinitionConfig definition, ProcessDBDictionary DBDictionary, boolean overwrite);
    void createOrUpdateProcessDictionaries(ProcessDefinitionConfig definition, List<ProcessDBDictionary> DBDictionaries, boolean overwrite);
    void updateDictionary(ProcessDBDictionary dictionary);
}
