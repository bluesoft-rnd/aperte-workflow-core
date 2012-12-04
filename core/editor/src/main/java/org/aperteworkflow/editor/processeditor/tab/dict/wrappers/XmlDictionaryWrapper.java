package org.aperteworkflow.editor.processeditor.tab.dict.wrappers;

import org.aperteworkflow.util.dict.wrappers.DictionaryWrapper;
import pl.net.bluesoft.rnd.processtool.dict.xml.Dictionary;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntry;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryPermission;
import pl.net.bluesoft.util.lang.cquery.func.F;
import pl.net.bluesoft.util.lang.cquery.func.P;

import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-12-03
 * Time: 16:16
 */
public class XmlDictionaryWrapper implements DictionaryWrapper<Dictionary> {
	public static final String _DICTIONARY_NAME = "dictionaryName";
	public static final String _DESCRIPTION = "description";
	public static final String _EDIT_PERMISSION = "editPermission";

	private final Dictionary dictionary;

	public XmlDictionaryWrapper(Dictionary dictionary) {
		this.dictionary = dictionary;
	}

	@Override
	public Dictionary getWrappedObject() {
		return dictionary;
	}

	@Override
	public String getDictionaryId() {
		return dictionary.getDictionaryId();
	}

	@Override
	public void setDictionaryId(String dictionaryId) {
		dictionary.setDictionaryId(dictionaryId);
	}

	@Override
	public String getLanguageCode() {
		return dictionary.getLanguageCode();
	}

	@Override
	public void setLanguageCode(String languageCode) {
		dictionary.setLanguageCode(languageCode);
	}

	@Override
	public String getDictionaryName() {
		return dictionary.getDictionaryName();
	}

	@Override
	public void setDictionaryName(String dictionaryName) {
		dictionary.setDictionaryName(dictionaryName);
	}

	@Override
	public String getDescription() {
		return dictionary.getDescription();
	}

	@Override
	public void setDescription(String description) {
		dictionary.setDescription(description);
	}

	public String getEditPermission() {
		DictionaryPermission permission = getEditDictionaryPermission();

		if (permission != null) {
			return permission.getRoleName();
		}
		return null;
	}

	public void setEditPermission(String editPermission) {
		DictionaryPermission permission = getEditDictionaryPermission();

		if (permission == null) {
			permission = new DictionaryPermission();
			permission.setPrivilegeName("EDIT");
			dictionary.getPermissions().add(permission);
		}
		permission.setRoleName(editPermission);
	}

	private DictionaryPermission getEditDictionaryPermission() {
		return from(dictionary.getPermissions()).firstOrDefault(new P<DictionaryPermission>() {
				@Override
				public boolean invoke(DictionaryPermission dictionaryPermission) {
					return dictionaryPermission.getPrivilegeName().equals("EDIT");
				}
			});
	}

	public List<XmlDictionaryItemWrapper> getItems() {
		return from(dictionary.getEntries()).select(new F<DictionaryEntry, XmlDictionaryItemWrapper>() {
			@Override
			public XmlDictionaryItemWrapper invoke(DictionaryEntry x) {
				return new XmlDictionaryItemWrapper(x);
			}
		}).toList();
	}

	public void addItem(XmlDictionaryItemWrapper item) {
		dictionary.getEntries().add(item.getWrappedObject());
	}

	public XmlDictionaryItemWrapper lookup(final String key) {
		DictionaryEntry dictionaryEntry = from(dictionary.getEntries()).firstOrDefault(new P<DictionaryEntry>() {
			@Override
			public boolean invoke(DictionaryEntry dictionaryEntry) {
				return dictionaryEntry.getKey().equals(key);
			}
		});
		return dictionaryEntry != null ? new XmlDictionaryItemWrapper(dictionaryEntry) : null;
	}
}
