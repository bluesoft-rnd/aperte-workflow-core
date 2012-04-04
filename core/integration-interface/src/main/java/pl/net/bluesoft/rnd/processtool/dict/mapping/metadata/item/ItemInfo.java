package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 15:48:27
 */
public class ItemInfo {
	private final Map<String, PropertyInfo> propInfo = new HashMap<String, PropertyInfo>();

	public PropertyInfo getPropertyInfo(String property) {
		return propInfo.get(property);
	}

	public void addPropertyInfo(PropertyInfo propertyInfo) {
		propInfo.put(propertyInfo.getProperty(), propertyInfo);
	}

	public Collection<PropertyInfo> getPropertyInfos() {
		return propInfo.values();
	}
}
