package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2014-06-13
 */
public class AuditLogDefinition {
	private final List<AuditLogGroup> groups = new ArrayList<AuditLogGroup>();

	public AuditLogDefinition addGroup(AuditLogGroup group) {
		groups.add(group);
		return this;
	}

	public AuditLogGroup findGroup(String key) {
		for (AuditLogGroup group : groups) {
			if (group.supports(key)) {
				return group;
			}
		}
		return null;
	}

	public AuditLogGroup findGroup(Class<? extends AbstractPersistentEntity> entityClass) {
		for (AuditLogGroup group : groups) {
			if (group.supports(entityClass)) {
				return group;
			}
		}
		return null;
	}
}
