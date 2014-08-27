package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

/**
 * User: POlszewski
 * Date: 2014-07-21
 */
public interface AuditContextChecker {
	boolean canBeLogged(IAttributesProvider provider);
}
