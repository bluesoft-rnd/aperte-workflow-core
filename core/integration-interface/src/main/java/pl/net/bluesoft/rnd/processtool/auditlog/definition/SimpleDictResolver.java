package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * User: POlszewski
 * Date: 2014-07-14
 */
public class SimpleDictResolver implements DictResolver {
	private final String dictAttribute;

	public SimpleDictResolver(String dictAttribute) {
		this.dictAttribute = dictAttribute;
	}

	@Override
	public String getDictKey(IAttributesProvider provider) {
		return provider.getSimpleAttributeValue(dictAttribute);
	}
}
