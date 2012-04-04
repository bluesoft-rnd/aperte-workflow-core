package pl.net.bluesoft.rnd.processtool.dict.mapping.providers;

import pl.net.bluesoft.rnd.processtool.dict.ProcessDictionaryRegistry;
import pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict.GlobalDictDescription;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;

/**
 * User: POlszewski
 * Date: 2012-01-03
 * Time: 23:41:45
 */
public class GlobalDictEntryProvider extends PTDictEntryProvider {
	public GlobalDictEntryProvider(GlobalDictDescription dictDesc) {
		super(dictDesc);
	}

	@Override
	protected ProcessDictionary getDictionary(ProcessDictionaryRegistry processDictionaryRegistry, DictEntryProviderParams params) {
		return processDictionaryRegistry.getSpecificOrDefaultGlobalDictionary(
				"db",
				dictDesc.getName(),
				params.getI18NSource().getLocale().toString()
		);
	}
}
