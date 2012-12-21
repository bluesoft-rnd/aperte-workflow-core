package pl.net.bluesoft.rnd.processtool.dict.mapping;

import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.CustomDictDescription;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.DictDescription;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.GlobalDictDescription;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.ProcessDictDescription;

import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 22:25:04
 */
public class DictDescriptionBuilder {
	private final Map<String, DictDescription> descs = new HashMap<String, DictDescription>();

	public <T extends DictDescription> T defineDictionary(String name, T desc) {
		descs.put(name, desc);
		return desc;
	}
	
	public ProcessDictDescription defineProcessDictionary(String name) {
		return defineDictionary(name, new ProcessDictDescription(name));
	}

	public ProcessDictDescription defineProcessDictionary(String name, Class<?> entryClass) {
		return (ProcessDictDescription)defineProcessDictionary(name).setEntryClass(entryClass);
	}

	public GlobalDictDescription defineGlobalDictionary(String name) {
		return defineDictionary(name, new GlobalDictDescription(name));
	}

	public GlobalDictDescription defineGlobalDictionary(String name, Class<?> entryClass) {
		return (GlobalDictDescription)defineGlobalDictionary(name).setEntryClass(entryClass);
	}

	public CustomDictDescription defineCustomDictionary(String name) {
		return defineDictionary(name, new CustomDictDescription(name));
	}

	public DictDescription getDictDescription(String dictName) {
		return descs.get(dictName);
	}
}
