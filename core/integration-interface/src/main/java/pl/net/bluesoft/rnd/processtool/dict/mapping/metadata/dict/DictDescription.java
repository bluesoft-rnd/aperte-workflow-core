package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict;

import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DictEntryProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 22:27:19
 */
public abstract class DictDescription {
	private final String name;
	private boolean lazyLoad = true;

	public DictDescription(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}

	public boolean isLazyLoad() {
		return lazyLoad;
	}

	public void setLazyLoad(boolean lazyLoad) {
		this.lazyLoad = lazyLoad;
	}

	public Collection<String> getBaseDictionaries() {
		return Collections.emptyList();
	}

	public abstract DictEntryProvider createDictEntryProvider();
}
