package org.aperteworkflow.editor.processeditor.tab.dict.wrappers;

import org.aperteworkflow.util.dict.wrappers.DictionaryItemExtensionWrapper;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntryExtension;

/**
 * User: POlszewski
 * Date: 2012-12-03
 * Time: 16:20
 */
public class XmlDictionaryItemExtensionWrapper implements DictionaryItemExtensionWrapper<DictionaryEntryExtension> {
	private final DictionaryEntryExtension extension;

	public XmlDictionaryItemExtensionWrapper(DictionaryEntryExtension extension) {
		this.extension = extension;
	}

	public XmlDictionaryItemExtensionWrapper() {
		this(new DictionaryEntryExtension());
	}

	@Override
	public DictionaryEntryExtension getWrappedObject() {
		return extension;
	}

	@Override
	public XmlDictionaryItemExtensionWrapper exactCopy() {
		XmlDictionaryItemExtensionWrapper copy = new XmlDictionaryItemExtensionWrapper();
		copy.setName(getName());
		copy.setValue(getValue());
		return copy;
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
