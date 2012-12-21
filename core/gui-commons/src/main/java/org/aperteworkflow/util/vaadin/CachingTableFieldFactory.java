package org.aperteworkflow.util.vaadin;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TableFieldFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-10-15
 * Time: 16:58
 */
public class CachingTableFieldFactory implements TableFieldFactory {
	private final Map<Object, Map<Object, Field>> fieldCache = new HashMap<Object, Map<Object, Field>>(); // propertyId -> objectId -> field

	private final TableFieldFactory fieldFactory;

	public CachingTableFieldFactory(TableFieldFactory fieldFactory) {
		this.fieldFactory = fieldFactory;
	}

	@Override
	public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
		Map<Object, Field> itemIdToFieldMap = fieldCache.get(propertyId);
		Field field;

		if (itemIdToFieldMap != null) {
			field = itemIdToFieldMap.get(itemId);
			if (field != null) {
				return field;
			}
		}
		else {
			itemIdToFieldMap = new HashMap<Object, Field>();
			fieldCache.put(propertyId, itemIdToFieldMap);
		}
		field = fieldFactory.createField(container, itemId, propertyId, uiContext);
		itemIdToFieldMap.put(itemId, field);
		return field;
	}

	public void invalidate(Object itemId, Object propertyId) {
		Map<Object, Field> itemIdToFieldMap = fieldCache.get(propertyId);
		if (itemIdToFieldMap != null) {
			itemIdToFieldMap.remove(itemId);
		}
	}

	public void invalidate(Object itemId) {
		for (Map<Object, Field> itemIdToFieldMap : fieldCache.values()) {
			itemIdToFieldMap.remove(itemId);
		}
	}

	public void invalidateAll() {
		for (Map<Object, Field> itemIdToFieldMap : fieldCache.values()) {
			itemIdToFieldMap.clear();
		}
	}
}
