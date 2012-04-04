package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.ProcessDictDescription;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 18:37:53
 */
public class ProcessDictEntryProvider extends PTDictEntryProvider {
	public ProcessDictEntryProvider(ProcessDictDescription dictDesc) {
		super(dictDesc);
	}

	@Override
	protected ProcessDictionary getDictionary(ProcessDictionaryRegistry processDictionaryRegistry, DictEntryProviderParams params) {
		return processDictionaryRegistry.getSpecificOrDefaultProcessDictionary(
				params.getProcessInstance().getDefinition(),
				"db",
				dictDesc.getName(),
				params.getI18NSource().getLocale().toString()
		);
	}
}
