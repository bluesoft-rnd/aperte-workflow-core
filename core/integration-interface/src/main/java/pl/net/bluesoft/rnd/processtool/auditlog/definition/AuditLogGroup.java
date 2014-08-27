package pl.net.bluesoft.rnd.processtool.auditlog.definition;

import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-06-13
 */
public class AuditLogGroup {
	private final String groupKey;
	private final String messageKey;
	private final boolean singleRow;

	private final Map<String, SimpleAuditConfig> simpleAttrConfigs = new LinkedHashMap<String, SimpleAuditConfig>();
	private final Map<Class, AuditedEntityHandler> entityConfigs = new LinkedHashMap<Class, AuditedEntityHandler>();

	public AuditLogGroup(String groupKey) {
		this(groupKey, groupKey, false);
	}

	public AuditLogGroup(String groupKey, boolean singleRow) {
		this(groupKey, groupKey, singleRow);
	}

	public AuditLogGroup(String groupKey, String messageKey) {
		this(groupKey, messageKey, false);
	}

	public AuditLogGroup(String groupKey, String messageKey, boolean singleRow) {
		this.groupKey = groupKey;
		this.messageKey = messageKey;
		this.singleRow = singleRow;
	}

	public String getGroupKey() {
		return groupKey;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public AuditLogGroup add(String attributeName) {
		return add(attributeName, null, null, null);
	}

	public AuditLogGroup add(String attributeName, String dictKey) {
		DirectDictResolver dictResolver = StringUtils.isNotEmpty(dictKey) ? new DirectDictResolver(dictKey) : null;
		return add(attributeName, dictResolver, null, null);
	}

	public AuditLogGroup add(String attributeName, DictResolver dictResolver) {
		return add(attributeName, dictResolver, null, null);
	}

	public AuditLogGroup add(String attributeName, DictResolver dictResolver, String annotation) {
		return add(attributeName, dictResolver, null, annotation);
	}

	public AuditLogGroup add(String attributeName, AuditContextChecker contextChecker) {
		return add(attributeName, null, contextChecker);
	}

	public AuditLogGroup add(String attributeName, DictResolver dictResolver, AuditContextChecker contextChecker) {
		return add(attributeName, dictResolver, contextChecker, null);
	}

	public AuditLogGroup add(String attributeName, DictResolver dictResolver, AuditContextChecker contextChecker, String annotation) {
		return add(new SimpleAuditConfig(attributeName, dictResolver, contextChecker, annotation));
	}

	private AuditLogGroup add(SimpleAuditConfig config) {
		if (simpleAttrConfigs.containsKey(config.attributeName)) {
			throw new IllegalArgumentException("Group " + groupKey + " has already attribute " + config);
		}
		simpleAttrConfigs.put(config.attributeName, config);
		return this;
	}

	public <T> AuditLogGroup add(Class<T> entityClass, AuditedEntityHandler<T> handler) {
		if (entityConfigs.containsKey(entityClass)) {
			throw new IllegalArgumentException("Group " + groupKey + " has already entity " + entityClass);
		}
		entityConfigs.put(entityClass, handler);
		return this;
	}

	public boolean supports(String key) {
		return getAuditConfig(key) != null;
	}

	public boolean supports(Class<? extends AbstractPersistentEntity> entityClass) {
		return getAuditedEntityHandler(entityClass) != null;
	}

	public SimpleAuditConfig getAuditConfig(String key) {
		return simpleAttrConfigs.get(key);
	}

	public <T extends AbstractPersistentEntity> AuditedEntityHandler getAuditedEntityHandler(Class<T> entityClass) {
		for (Map.Entry<Class, AuditedEntityHandler> entry : entityConfigs.entrySet()) {
			if (entry.getKey().isAssignableFrom(entityClass)) {
				return entry.getValue();
			}
		}
		return null;
	}

	public boolean isSingleRow() {
		return singleRow;
	}
}
