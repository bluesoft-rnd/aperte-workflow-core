package pl.net.bluesoft.rnd.processtool.auditlog;

import pl.net.bluesoft.rnd.processtool.auditlog.definition.AuditLogDefinition;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2014-06-12
 */
public interface AuditLogHandler {
	AuditLogDefinition getAuditLogDefnition(IAttributesProvider provider);

	void postProcess(IAttributesProvider provider, List<HandlingResult> auditLogs);
}
