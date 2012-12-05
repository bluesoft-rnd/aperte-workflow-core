package pl.net.bluesoft.rnd.processtool.ui.dict.wrappers;

import org.aperteworkflow.util.dict.wrappers.DictionaryItemExtensionWrapper;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 12:48
 */
public class DBDictionaryItemExtensionWrapper implements DictionaryItemExtensionWrapper<ProcessDBDictionaryItemExtension> {
	private final ProcessDBDictionaryItemExtension extension;

	public DBDictionaryItemExtensionWrapper() {
		this(new ProcessDBDictionaryItemExtension());
	}

	public DBDictionaryItemExtensionWrapper(ProcessDBDictionaryItemExtension extension) {
		this.extension = extension;
	}

	@Override
	public ProcessDBDictionaryItemExtension getWrappedObject() {
		return extension;
	}

	@Override
	public DBDictionaryItemExtensionWrapper exactCopy() {
		return new DBDictionaryItemExtensionWrapper(extension.exactCopy());
	}

	@Override
	public String getName() {
		return extension.getName();
	}

	@Override
	public void setName(String name) {
		extension.setName(name);
	}

	@Override
	public String getValue() {
		return extension.getValue();
	}

	@Override
	public void setValue(String value) {
		extension.setValue(value);
	}

	@Override
	public String getDescription() {
		return extension.getDescription();
	}
}
