package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * User: POlszewski
 * Date: 2014-07-14
 */
public class DirectDictResolver implements DictResolver {
	private final String dictName;

	public DirectDictResolver(String dictName) {
		this.dictName = dictName;
	}

	@Override
	public String getDictKey(IAttributesProvider provider) {
		return dictName;
	}
}
