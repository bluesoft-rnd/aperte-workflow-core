package pl.net.bluesoft.rnd.processtool.dict;

import com.thoughtworks.xstream.converters.basic.DateConverter;
import pl.net.bluesoft.rnd.processtool.dict.exception.DictionaryLoadingException;
import pl.net.bluesoft.rnd.processtool.dict.xml.Dictionary;
import pl.net.bluesoft.rnd.processtool.dict.xml.*;
import pl.net.bluesoft.rnd.processtool.model.dict.db.*;
import pl.net.bluesoft.rnd.pt.utils.xml.OXHelper;
import pl.net.bluesoft.util.lang.Strings;

import java.util.*;

public class DictionaryLoader extends OXHelper {
    private static DictionaryLoader instance = new DictionaryLoader();

    public static DictionaryLoader getInstance() {
        return instance;
    }

    private DictionaryLoader() {
        registerConverter(new DateConverter("yyyy-MM-dd", new String[] {"yyyy/MM/dd", "dd-MM-yyyy", "dd/MM/yyyy"}));
    }

    @Override
    protected Class[] getSupportedClasses() {
        return new Class[] { 
                Dictionary.class,
                DictionaryPermission.class,
                DictionaryEntry.class,
                DictionaryEntryExtension.class,
                DictionaryEntryValue.class,
                ProcessDictionaries.class
        };
    }

    public static List<ProcessDBDictionary> getDictionariesFromXML(ProcessDictionaries processDictionaries) {
        List<ProcessDBDictionary> result = new ArrayList<ProcessDBDictionary>();

		for (Dictionary dict : processDictionaries.getDictionaries()) {
			result.add(createDbDict(processDictionaries, dict));
        }
        return result;
    }

	private static ProcessDBDictionary createDbDict(ProcessDictionaries processDictionaries, Dictionary dict) {
		ProcessDBDictionary dbDict = new ProcessDBDictionary();

		dbDict.setDictionaryId(dict.getId());
		dbDict.setDefaultName(dict.getName());

		for (DictionaryI18N localizedName : dict.getLocalizedNames()) {
			dbDict.setName(localizedName.getLang(), localizedName.getValue());
		}

		dbDict.setDescription(dict.getDescription());

		for (DictionaryPermission permission : getPermissions(processDictionaries, dict)) {
			dbDict.addPermission(createDbPermission(permission));
		}

		if (dict.getDefaultExtensions() != null) {
			for (DictionaryDefaultEntryExtension defaultExt : dict.getDefaultExtensions()) {
				dbDict.addDefaultExtension(createDbDefaultExt(defaultExt));
			}
		}

		for (DictionaryEntry entry : dict.getEntries()) {
			dbDict.addItem(createDbItem(dbDict, entry));
		}

		return dbDict;
	}

	private static List<DictionaryPermission> getPermissions(ProcessDictionaries processDictionaries, Dictionary dict) {
		return dict.getPermissions().isEmpty() ? processDictionaries.getPermissions() : dict.getPermissions();
	}

	private static ProcessDBDictionaryItem createDbItem(ProcessDBDictionary dbDict, DictionaryEntry entry) {
		ProcessDBDictionaryItem dbItem = new ProcessDBDictionaryItem();

		dbItem.setDefaultDescription(entry.getDescription());
        for (DictionaryI18N localizedDescription : entry.getLocalizedDescriptions()) {
            dbItem.setDescription(localizedDescription.getLang(), localizedDescription.getValue());
        }


        dbItem.setKey(entry.getKey());
		dbItem.setValueType(entry.getValueType());

		for (DictionaryEntryValue val : entry.getValues()) {
			dbItem.addValue(createDbValue(dbDict, val));
		}
		return dbItem;
	}

	private static ProcessDBDictionaryItemValue createDbValue(ProcessDBDictionary dbDict, DictionaryEntryValue val) {
		ProcessDBDictionaryItemValue dbValue = new ProcessDBDictionaryItemValue();

		dbValue.setDefaultValue(val.getValue());

		for (DictionaryI18N localizedValue : val.getLocalizedValues()) {
			dbValue.setValue(localizedValue.getLang(), localizedValue.getValue());
		}

		if (val.getValidDay() != null) {
			dbValue.setValidityDates(val.getValidDay(), val.getValidDay());
		}
		else {
			dbValue.setValidityDates(val.getValidFrom(), val.getValidTo());
		}

		dbDict.initValueExtensions(dbValue);

		for (DictionaryEntryExtension ext : val.getExtensions()) {
			dbValue.addOrUpdateExtension(createDbExt(ext));
		}
		return dbValue;
	}

	private static ProcessDBDictionaryPermission createDbPermission(DictionaryPermission permission) {
		ProcessDBDictionaryPermission dbPerm = new ProcessDBDictionaryPermission();
		dbPerm.setPrivilegeName(permission.getPrivilegeName());
		dbPerm.setRoleName(permission.getRoleName());
		return dbPerm;
	}

	private static ProcessDBDictionaryItemExtension createDbExt(DictionaryEntryExtension ext) {
		ProcessDBDictionaryItemExtension dbItemExt = new ProcessDBDictionaryItemExtension();
		dbItemExt.setName(ext.getName());
		dbItemExt.setValue(ext.getValue());
		dbItemExt.setValueType(ext.getValueType());
		dbItemExt.setDescription(ext.getDescription());
		return dbItemExt;
	}

	private static ProcessDBDictionaryDefaultItemExtension createDbDefaultExt(DictionaryDefaultEntryExtension ext) {
		ProcessDBDictionaryDefaultItemExtension dbItemExt = new ProcessDBDictionaryDefaultItemExtension();
		dbItemExt.setName(ext.getName());
		dbItemExt.setValue(ext.getValue());
		dbItemExt.setValueType(ext.getValueType());
		dbItemExt.setDescription(ext.getDescription());
		return dbItemExt;
	}

	public static void validateDictionaries(List<ProcessDBDictionary> processDBDictionaries) {
        StringBuilder sb = new StringBuilder();
        Set<String> hashSet = new HashSet<String>();
        for (ProcessDBDictionary dict : processDBDictionaries) {
            if (!Strings.hasText(dict.getDictionaryId())) {
                sb.append("Empty dictionary name").append("\n");
                continue;
            }
            String hash = '(' + dict.getDictionaryId() + ')';
            if (hashSet.contains(hash)) {
                sb.append("Duplicated dictionary definition: ").append(hash).append('\n');
                continue;
            }
            hashSet.add(hash);
			for (ProcessDBDictionaryItem item : dict.getItems().values()) {
                if (!Strings.hasText(item.getKey())) {
                    sb.append(hash).append(": empty item key").append('\n');
                    continue;
                }
                if (item.getValues().isEmpty()) {
                    sb.append(hash).append(": empty values set for key: ").append(item.getKey()).append('\n');
                    continue;
                }
                for (ProcessDBDictionaryItemValue val : item.getValues()) {
                    if (!Strings.hasText(val.getDefaultValue())) {
                        sb.append(hash).append(": empty value for key: ").append(item.getKey()).append('\n');
                        continue;
                    }
                    Date startDate = val.getValidFrom();
                    Date endDate = val.getValidTo();
                    if (endDate != null && startDate != null && endDate.before(startDate)) {
                        sb.append(hash).append(": wrong date ranges in: ").append(val.getDefaultValue()).append(" for key: ").append(item.getKey()).append("\n");
                    }
                }
                StringBuilder dateSb = new StringBuilder();
                boolean startDateFullRange = false, endDateFullRange = false;
                for (ProcessDBDictionaryItemValue val : item.getValues()) {
                    startDateFullRange = validateSingleDate(dateSb, hash, item, startDateFullRange, val, val.getValidFrom());
                    endDateFullRange = validateSingleDate(dateSb, hash, item, endDateFullRange, val, val.getValidTo());
                    if (!dateSb.toString().isEmpty()) {
                        break;
                    }
                }
                sb.append(dateSb);
            }
        }
        if (!sb.toString().isEmpty()) {
            throw new DictionaryLoadingException(sb.toString());
        }
    }

    private static boolean validateSingleDate(StringBuilder sb, String hash, ProcessDBDictionaryItem item,
                                              boolean fullRangeFound, ProcessDBDictionaryItemValue val, Date date) {
        if (date == null) {
            if (fullRangeFound) {
                sb.append(hash).append(": duplicated full date range for key: ").append(item.getKey());
            }
            else {
                fullRangeFound = true;
            }
        }
        else {
            for (ProcessDBDictionaryItemValue otherVal : item.getValues()) {
                if (val != otherVal && otherVal.isValidForDate(date)) {
                    sb.append(hash).append(": overlapping value dates for key: ").append(item.getKey());
                }
            }
        }
        return fullRangeFound;
    }
}
