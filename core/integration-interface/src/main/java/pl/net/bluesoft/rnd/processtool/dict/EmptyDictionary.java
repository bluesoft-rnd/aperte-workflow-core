package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;

import java.util.Collection;
import java.util.Collections;

public class EmptyDictionary implements ProcessDictionary {

    @Override
    public String getDictionaryId() {
        return getClass().getName();
    }

    @Override
    public String getDictionaryName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getLanguageCode() {
        return null;
    }

    @Override
    public Boolean isDefaultDictionary() {
        return false;
    }

    @Override
    public ProcessDictionaryItem lookup(Object key) {
        return null;
    }

    @Override
    public Collection itemKeys() {
        return Collections.emptyList();
    }

    @Override
    public Collection items() {
        return Collections.emptyList();
    }

	@Override
	public boolean containsKey(Object key) {
		return false;
	}

	@Override
	public ProcessDefinitionConfig getProcessDefinition() {
		return null;
	}
}
