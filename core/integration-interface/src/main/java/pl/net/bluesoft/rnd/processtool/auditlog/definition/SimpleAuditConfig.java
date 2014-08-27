package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

public class SimpleAuditConfig {
	public final String attributeName;
	public final DictResolver dictResolver;
	public final AuditContextChecker contextChecker;
	public final String annotation;

	public SimpleAuditConfig(String attributeName, DictResolver dictResolver, AuditContextChecker contextChecker, String annotation) {
		this.attributeName = attributeName;
		this.dictResolver = dictResolver;
		this.contextChecker = contextChecker;
		this.annotation = annotation;
	}

	public String getDictKey(IAttributesProvider attributesProvider) {
		return dictResolver != null ? dictResolver.getDictKey(attributesProvider) : null;
	}

	public boolean canBeLogged(IAttributesProvider attributesProvider) {
		return contextChecker == null || contextChecker.canBeLogged(attributesProvider);
	}
}