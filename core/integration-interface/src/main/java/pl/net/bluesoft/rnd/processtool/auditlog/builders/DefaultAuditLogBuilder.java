package pl.net.bluesoft.rnd.processtool.auditlog.builders;

import pl.net.bluesoft.rnd.processtool.auditlog.definition.*;
import pl.net.bluesoft.rnd.processtool.auditlog.model.AuditLog;
import pl.net.bluesoft.rnd.processtool.auditlog.model.AuditedProperty;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.util.lang.Pair;

import java.util.*;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * User: POlszewski
 * Date: 2014-06-13
 */
public class DefaultAuditLogBuilder implements AuditLogBuilder {
	private final AuditLogDefinition definition;
	private final IAttributesProvider provider;

	private final Map<Pair<String, Object>, AuditLog> map = new HashMap<Pair<String, Object>, AuditLog>();
	private final List<AuditLog> list = new ArrayList<AuditLog>();


	public DefaultAuditLogBuilder(AuditLogDefinition definition, IAttributesProvider provider) {
		this.definition = definition;
		this.provider = provider;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public void addSimple(String key, String oldValue, String newValue) {
		AuditLogGroup group = definition.findGroup(key);

		if (group != null) {
			SimpleAuditConfig auditConfig = group.getAuditConfig(key);

			if (auditConfig.canBeLogged(provider)) {
				AuditLog auditLog = getAuditLog(group.getGroupKey(), null, group.isSingleRow());
				String messageKey = getMessageKey(group, key);
				String dictKey = auditConfig.getDictKey(provider);
				String annotation = auditConfig.annotation;

				auditLog.addPre(new AuditedProperty(messageKey, key, blankToNull(oldValue), dictKey, annotation));
				auditLog.addPost(new AuditedProperty(messageKey, key, blankToNull(newValue), dictKey, annotation));
			}
		}
	}

	@Override
	public <T extends AbstractPersistentEntity> void addPre(T entry) {
		addPre(Collections.singleton(entry));
	}

	@Override
	public <T extends AbstractPersistentEntity> void addPost(T entry) {
		addPost(Collections.singleton(entry));
	}

	private static String blankToNull(String value) {
		return hasText(value) ? value : null;
	}

	@Override
	public <T extends AbstractPersistentEntity> void addPre(Collection<T> entities) {
		add(LogTarget.PRE, entities);
	}

	@Override
	public <T extends AbstractPersistentEntity> void addPost(Collection<T> entries) {
		add(LogTarget.POST, entries);
	}

	private enum LogTarget {
		PRE, POST
	}

	private <T extends AbstractPersistentEntity> void add(final LogTarget logTarget, Collection<T> entities) {
		if (entities == null || entities.isEmpty()) {
			return;
		}

		for (T entity : entities) {
			final AuditLogGroup group = definition.findGroup(entity.getClass());

			if (group != null) {
				AuditedEntityHandler<T> handler = group.getAuditedEntityHandler(entity.getClass());
				final AuditLog auditLog = getAuditLog(group.getGroupKey(), entity, group.isSingleRow());

				handler.auditLog(entity, new AuditedEntityCallback() {
					@Override
					public void add(String name, String value) {
						add(name, value, null, null, null);
					}

					@Override
					public void add(String name, String value, String dictKey) {
						add(name, value, new DirectDictResolver(dictKey), null,  null);
					}

					@Override
					public void add(String name, String value, DictResolver dictResolver) {
						add(name, value, dictResolver, null, null);
					}

					@Override
					public void add(String name, String value, DictResolver dictResolver, String annotation) {
						add(name, value, dictResolver, null, annotation);
					}

					@Override
					public void add(String name, String value, DictResolver dictResolver, AuditContextChecker contextChecker) {
						add(name, value, dictResolver, contextChecker, null);
					}

					@Override
					public void add(String name, String value, DictResolver dictResolver, AuditContextChecker contextChecker, String annotation) {
						if (contextChecker != null && !contextChecker.canBeLogged(provider)) {
							return;
						}

						String messageKey = getMessageKey(group, name);
						String dictKey = dictResolver != null ? dictResolver.getDictKey(provider) : null;
						AuditedProperty property = new AuditedProperty(messageKey, name, value, dictKey, annotation);

						if (logTarget == LogTarget.PRE) {
							auditLog.addPre(property);
						}
						else {
							auditLog.addPost(property);
						}
					}
				});
			}
		}
	}

	private static String getMessageKey(AuditLogGroup group, String name) {
		return group.getMessageKey() + '.' + name;
	}

	private AuditLog getAuditLog(String groupKey, Object object, boolean singleRow) {
		Object objectId = getObjectId(object);
		Pair<String, Object> key = new Pair<String, Object>(groupKey, objectId);

		AuditLog enityLog = map.get(key);

		if(enityLog == null){
			enityLog = new AuditLog(groupKey);
			enityLog.setSingleRow(singleRow);
			map.put(key, enityLog);
			list.add(enityLog);
		}
		return enityLog;
	}

	private static Object getObjectId(Object object) {
		Object objectId = null;

		if (object instanceof AbstractPersistentEntity) {
			objectId = ((AbstractPersistentEntity)object).getId();
		}

		if (objectId == null) {
			objectId = object;
		}
		return objectId;
	}

	@Override
	public List<AuditLog> toAuditLogs() {
		List<AuditLog> result = new ArrayList<AuditLog>(list.size());

		for (AuditLog enityLog : list) {
			if (enityLog.isDifferent()) {
				result.add(enityLog);
			}
		}
		return result;
	}
}
