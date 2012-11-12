package org.aperteworkflow.util.vaadin;

import com.vaadin.ui.Table;

import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-10-15
 * Time: 16:58
 */
public class CachingTableColumnGenerator implements Table.ColumnGenerator {
	private final Map<Object, Map<Object, Object>> cellContentsCache = new HashMap<Object, Map<Object, Object>>(); // columnId -> objectId -> component/string

	private final Table.ColumnGenerator columnGenerator;

	public CachingTableColumnGenerator(Table.ColumnGenerator columnGenerator) {
		this.columnGenerator = columnGenerator;
	}

	@Override
	public Object generateCell(Table source, Object itemId, Object columnId) {
		Map<Object, Object> itemIdToCellContentMap = cellContentsCache.get(columnId);
		Object cellContent;

		if (itemIdToCellContentMap != null) {
			cellContent = itemIdToCellContentMap.get(itemId);
			if (cellContent != null) {
				return cellContent;
			}
		}
		else {
			itemIdToCellContentMap = new HashMap<Object, Object>();
			cellContentsCache.put(columnId, itemIdToCellContentMap);
		}
		cellContent = columnGenerator.generateCell(source, itemId, columnId);
		itemIdToCellContentMap.put(itemId, cellContent);
		return cellContent;
	}

	public void invalidate(Object itemId, Object propertyId) {
		Map<Object, Object> itemIdToCellContentMap = cellContentsCache.get(propertyId);
		if (itemIdToCellContentMap != null) {
			itemIdToCellContentMap.remove(itemId);
		}
	}

	public void invalidate(Object itemId) {
		for (Map<Object, Object> itemIdToCellContentMap : cellContentsCache.values()) {
			itemIdToCellContentMap.remove(itemId);
		}
	}

	public void invalidateAll() {
		for (Map<Object, Object> itemIdToCellContentMap : cellContentsCache.values()) {
			itemIdToCellContentMap.clear();
		}
	}
}
