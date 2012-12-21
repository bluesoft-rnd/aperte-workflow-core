package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict;

import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DictEntryProvider;
import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.GlobalDictEntryProvider;

/**
 * User: POlszewski
 * Date: 2012-01-03
 * Time: 23:37:55
 */
public class GlobalDictDescription extends PTDictDescription {
	public GlobalDictDescription(String name) {
		super(name);
	}

	@Override
	public DictEntryProvider createDictEntryProvider() {
		return new GlobalDictEntryProvider(this);
	}
}
