package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * User: POlszewski
 * Date: 2014-07-14
 */
public interface DictResolver {
	String getDictKey(IAttributesProvider provider);
}
