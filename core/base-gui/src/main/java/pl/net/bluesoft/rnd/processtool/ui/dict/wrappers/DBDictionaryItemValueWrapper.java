package pl.net.bluesoft.rnd.processtool.ui.dict.wrappers;

import org.aperteworkflow.util.dict.wrappers.DictionaryItemValueWrapper;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 12:57
 */
public class DBDictionaryItemValueWrapper implements DictionaryItemValueWrapper<ProcessDBDictionaryItemValue, DBDictionaryItemExtensionWrapper> {
	private final ProcessDBDictionaryItemValue itemValue;

	public DBDictionaryItemValueWrapper() {
		this(new ProcessDBDictionaryItemValue());
	}

	public DBDictionaryItemValueWrapper(ProcessDBDictionaryItemValue itemValue) {
		this.itemValue = itemValue;
	}

	@Override
	public ProcessDBDictionaryItemValue getWrappedObject() {
		return itemValue;
	}

	@Override
	public DBDictionaryItemValueWrapper exactCopy() {
		return new DBDictionaryItemValueWrapper(itemValue.exactCopy());
	}

	@Override
	public DBDictionaryItemValueWrapper shallowCopy() {
		return new DBDictionaryItemValueWrapper(itemValue.shallowCopy());
	}

	@Override
	public String getValue() {
		return itemValue.getValue();
	}

	@Override
	public void setValue(String value) {
		itemValue.setValue(value);
	}

	@Override
	public Date getValidStartDate() {
		return itemValue.getValidStartDate();
	}

	@Override
	public void setValidStartDate(Date validStartDate) {
		itemValue.setValidStartDate(validStartDate);
	}

	@Override
	public Date getValidEndDate() {
		return itemValue.getValidEndDate();
	}

	@Override
	public void setValidEndDate(Date validEndDate) {
		itemValue.setValidEndDate(validEndDate);
	}

	@Override
	public boolean isValidForDate(Date date) {
		return itemValue.isValidForDate(date);
	}

//	@Override
//	public Map<String, DBDictionaryItemExtensionWrapper> getExtensions() {
//		if (extensions == null) {
//			Map<String, ProcessDBDictionaryItemExtension> unwrappedExtensions = itemValue.getExtensions();
//			if (unwrappedExtensions != null) {
//				extensions = new HashMap<String, DBDictionaryItemExtensionWrapper>();
//
//				for (Map.Entry<String, ProcessDBDictionaryItemExtension> entry : unwrappedExtensions.entrySet()) {
//					extensions.put(entry.getKey(), new DBDictionaryItemExtensionWrapper(entry.getValue()));
//				}
//			}
//		}
//		return extensions;
//	}
//
//	@Override
//	public void setExtensions(Map<String, DBDictionaryItemExtensionWrapper> extensions) {
//		if (extensions != null) {
//			Map<String, ProcessDBDictionaryItemExtension> unwrappedExtensions = new HashMap<String, ProcessDBDictionaryItemExtension>();
//
//			for (Map.Entry<String, DBDictionaryItemExtensionWrapper> entry : extensions.entrySet()) {
//				unwrappedExtensions.put(entry.getKey(), entry.getValue().getWrappedObject());
//			}
//			itemValue.setExtensions(unwrappedExtensions);
//		}
//		else {
//			itemValue.setExtensions(null);
//		}
//		this.extensions = extensions;
//	}

	@Override
	public Map<String, DBDictionaryItemExtensionWrapper> getExtensions() {
		Map<String,ProcessDBDictionaryItemExtension> extensions = itemValue.getExtensions();
		Map<String, DBDictionaryItemExtensionWrapper> extensionWrappers = new HashMap<String, DBDictionaryItemExtensionWrapper>();

		for (Map.Entry<String, ProcessDBDictionaryItemExtension> entry : extensions.entrySet()) {
			extensionWrappers.put(entry.getKey(), new DBDictionaryItemExtensionWrapper(entry.getValue()));
		}
		return extensionWrappers;
	}

	@Override
	public void setExtensions(Map<String, DBDictionaryItemExtensionWrapper> extensions) {
		Map<String, ProcessDBDictionaryItemExtension> unwrappedExtensions = new HashMap<String, ProcessDBDictionaryItemExtension>();

		for (Map.Entry<String, DBDictionaryItemExtensionWrapper> entry : extensions.entrySet()) {
			unwrappedExtensions.put(entry.getKey(), entry.getValue().getWrappedObject());
		}
		itemValue.setExtensions(unwrappedExtensions);
	}

	@Override
	public Collection<String> getExtensionNames() {
		return itemValue.getExtensionNames();
	}

	@Override
	public DBDictionaryItemExtensionWrapper getExtensionByName(String extensionName) {
		ProcessDBDictionaryItemExtension unwrappedExt = itemValue.getExtensionByName(extensionName);
		return unwrappedExt != null ? new DBDictionaryItemExtensionWrapper(unwrappedExt) : null;
	}

	@Override
	public boolean hasFullDatesRange() {
		return itemValue.hasFullDatesRange();
	}
}
