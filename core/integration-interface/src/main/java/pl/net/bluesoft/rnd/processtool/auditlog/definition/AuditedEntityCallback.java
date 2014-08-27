package pl.net.bluesoft.rnd.processtool.auditlog.definition;

/**
 * User: POlszewski
 * Date: 2014-06-15
 */
public interface AuditedEntityCallback {
	void add(String name, String value);
	void add(String name, String value, String dictKey);
	void add(String name, String value, DictResolver dictResolver);
	void add(String name, String value, DictResolver dictResolver, String annotation);
	void add(String name, String value, DictResolver dictResolver, AuditContextChecker contextChecker);
	void add(String name, String value, DictResolver dictResolver, AuditContextChecker contextChecker, String annotation);
}
