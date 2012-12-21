package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict;

import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DictEntryProvider;
import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.ProcessDictEntryProvider;

/**
 * User: POlszewski
 * Date: 2012-01-03
 * Time: 23:37:04
 */
public class ProcessDictDescription extends PTDictDescription {
	public ProcessDictDescription(String name) {
		super(name);
	}

	@Override
	public DictEntryProvider createDictEntryProvider() {
		return new ProcessDictEntryProvider(this);
	}
}
