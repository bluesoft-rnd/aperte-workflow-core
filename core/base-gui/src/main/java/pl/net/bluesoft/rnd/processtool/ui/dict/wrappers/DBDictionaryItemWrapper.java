package pl.net.bluesoft.rnd.processtool.ui.dict.wrappers;

import org.aperteworkflow.util.dict.wrappers.DictionaryItemWrapper;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 13:21
 */
public class DBDictionaryItemWrapper implements DictionaryItemWrapper<ProcessDBDictionaryItem, DBDictionaryItemValueWrapper> {
	private final ProcessDBDictionaryItem item;

	public DBDictionaryItemWrapper() {
		this(new ProcessDBDictionaryItem());
	}

	public DBDictionaryItemWrapper(ProcessDBDictionaryItem item) {
		this.item = item;
	}

	@Override
	public ProcessDBDictionaryItem getWrappedObject() {
		return item;
	}

	@Override
	public String getDescription() {
		return item.getDescription();
	}

	@Override
	public void setDescription(String description) {
		item.setDescription(description);
	}

	@Override
	public String getKey() {
		return item.getKey();
	}

	@Override
	public void setKey(String key) {
		item.setKey(key);
	}

	@Override
	public String getValueType() {
		return item.getValueType();
	}

	@Override
	public void setValueType(String valueType) {
		item.setValueType(valueType);
	}

//	@Override
//	public Set<DBDictionaryItemValueWrapper> getValues() {
//		if (values == null && item.getValues() != null) {
//			values = new HashSet<DBDictionaryItemValueWrapper>();
//
//			for (ProcessDBDictionaryItemValue value : item.getValues()) {
//				values.add(getWrapper(value));
//			}
//		}
//		return values;
//	}
//
//	@Override
//	public void setValues(Set<DBDictionaryItemValueWrapper> values) {
//		if (values != null) {
//			Set<ProcessDBDictionaryItemValue> unwrappedValues = new HashSet<ProcessDBDictionaryItemValue>();
//
//			for (DBDictionaryItemValueWrapper value : values) {
//				unwrappedValues.add(value.getWrappedObject());
//			}
//			item.setValues(unwrappedValues);
//		}
//		else {
//			item.setValues(null);
//		}
//		this.values = values;
//	}
//
//	private DBDictionaryItemValueWrapper getWrapper(ProcessDBDictionaryItemValue value) {
//		if (value != null) {
//			DBDictionaryItemValueWrapper wrapper = identityMap.get(value);
//			if (wrapper == null) {
//				wrapper = new DBDictionaryItemValueWrapper(value);
//			}
//			return wrapper;
//		}
//		return null;
//	}

	@Override
	public Set<DBDictionaryItemValueWrapper> getValues() {
		Set<DBDictionaryItemValueWrapper> values = new HashSet<DBDictionaryItemValueWrapper>();
		for (ProcessDBDictionaryItemValue value : item.getValues()) {
			values.add(new DBDictionaryItemValueWrapper(value));
		}
		return values;
	}

	@Override
	public void setValues(Set<DBDictionaryItemValueWrapper> values) {
		Set<ProcessDBDictionaryItemValue> unwrappedValues = new HashSet<ProcessDBDictionaryItemValue>();
		for (DBDictionaryItemValueWrapper value : values) {
			unwrappedValues.add(value.getWrappedObject());
		}
		item.setValues(unwrappedValues);
	}
}
