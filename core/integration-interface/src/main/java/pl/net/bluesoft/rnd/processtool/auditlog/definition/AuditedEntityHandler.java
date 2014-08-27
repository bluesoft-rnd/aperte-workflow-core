package pl.net.bluesoft.rnd.processtool.auditlog.definition;

/**
 * User: POlszewski
 * Date: 2014-06-13
 */
public interface AuditedEntityHandler<EntityType> {
	void auditLog(EntityType entity, AuditedEntityCallback callback);
}
