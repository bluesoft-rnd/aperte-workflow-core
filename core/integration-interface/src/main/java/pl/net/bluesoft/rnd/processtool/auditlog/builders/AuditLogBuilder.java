package pl.net.bluesoft.rnd.processtool.auditlog.builders;

import pl.net.bluesoft.rnd.processtool.auditlog.model.AuditLog;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2014-06-12
 */
public interface AuditLogBuilder {
	boolean isNull();
	void addSimple(String key, String oldValue, String newValue);
	<T extends AbstractPersistentEntity> void addPre(T entry);
	<T extends AbstractPersistentEntity> void addPost(T entry);
	<T extends AbstractPersistentEntity> void addPre(Collection<T> entries);
	<T extends AbstractPersistentEntity> void addPost(Collection<T> entries);

	List<AuditLog> toAuditLogs();

	AuditLogBuilder NULL = new AuditLogBuilder() {
		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public void addSimple(String key, String oldValue, String newValue) {}

		@Override
		public <T extends AbstractPersistentEntity> void addPre(T entry) {}

		@Override
		public <T extends AbstractPersistentEntity> void addPost(T entry) {}

		@Override
		public <T extends AbstractPersistentEntity> void addPre(Collection<T> entries) {}

		@Override
		public <T extends AbstractPersistentEntity> void addPost(Collection<T> entries) {}

		@Override
		public List<AuditLog> toAuditLogs() {
			return Collections.emptyList();
		}
	};
}
