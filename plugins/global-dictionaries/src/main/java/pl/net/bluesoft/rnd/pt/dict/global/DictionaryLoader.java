package pl.net.bluesoft.rnd.pt.dict.global;

import com.thoughtworks.xstream.converters.basic.DateConverter;
import pl.net.bluesoft.rnd.processtool.dict.exception.DictionaryLoadingException;
import pl.net.bluesoft.rnd.processtool.model.dict.db.*;
import pl.net.bluesoft.rnd.pt.dict.global.xml.*;
import pl.net.bluesoft.rnd.pt.dict.global.xml.Dictionary;
import pl.net.bluesoft.rnd.pt.utils.xml.OXHelper;
import pl.net.bluesoft.util.lang.Strings;

import java.util.*;


public class DictionaryLoader extends OXHelper {
    private static DictionaryLoader instance = new DictionaryLoader();

    public static DictionaryLoader getInstance() {
        return instance;
    }

    private DictionaryLoader() {
        super();
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
            ProcessDBDictionary dbDict = new ProcessDBDictionary();
            dbDict.setDescription(dict.getDescription());
            dbDict.setDictionaryId(dict.getDictionaryId());
            dbDict.setDictionaryName(dict.getDictionaryName());
            dbDict.setLanguageCode(dict.getLanguageCode());

            List<DictionaryPermission> permissions = dict.getPermissions().isEmpty() ? processDictionaries.getPermissions() : dict.getPermissions();
            for (DictionaryPermission permission : permissions) {
                ProcessDBDictionaryPermission dbPerm = new ProcessDBDictionaryPermission();
                dbPerm.setPrivilegeName(permission.getPrivilegeName());
                dbPerm.setRoleName(permission.getRoleName());
                dbDict.addPermission(dbPerm);
            }

            for (DictionaryEntry entry : dict.getEntries()) {
                ProcessDBDictionaryItem dbItem = new ProcessDBDictionaryItem();
                dbItem.setDescription(entry.getDescription());
                dbItem.setKey(entry.getKey());
                dbItem.setValueType(entry.getValueType());
                for (DictionaryEntryValue val : entry.getValues()) {
                    ProcessDBDictionaryItemValue dbValue = new ProcessDBDictionaryItemValue();
                    dbValue.setValue(val.getValue());
                    if (val.getValidSingleDate() != null) {
                        dbValue.setValidityDates(val.getValidSingleDate(), val.getValidSingleDate());
                    }
                    else {
                        dbValue.setValidityDates(val.getValidStartDate(), val.getValidEndDate());
                    }
                    for (DictionaryEntryExtension ext : val.getExtensions()) {
                        ProcessDBDictionaryItemExtension dbItemExt = new ProcessDBDictionaryItemExtension();
                        dbItemExt.setName(ext.getName());
                        dbItemExt.setValue(ext.getValue());
                        dbItemExt.setValueType(ext.getValueType());
                        dbItemExt.setDescription(ext.getDescription());
                        dbValue.addItemExtension(dbItemExt);
                    }
                    dbItem.addValue(dbValue);
                }
                dbDict.addItem(dbItem);
            }
            dbDict.setDefaultDictionary(Strings.hasText(processDictionaries.getDefaultLanguage())
                    && processDictionaries.getDefaultLanguage().equals(dbDict.getLanguageCode()));
            result.add(dbDict);
        }
        return result;
    }

    public static void validateDictionaries(List<ProcessDBDictionary> processDBDictionaries) {
        StringBuilder sb = new StringBuilder();
        Set<String> hashSet = new HashSet<String>();
        for (ProcessDBDictionary dict : processDBDictionaries) {
            if (!Strings.hasText(dict.getDictionaryId())) {
                sb.append("Empty dictionary name").append("\n");
                continue;
            }
            if (!Strings.hasText(dict.getLanguageCode())) {
                sb.append("Unspecified language code for dictionary: ").append(dict.getDictionaryId()).append("\n");
                continue;
            }
            String hash = "(" + dict.getDictionaryId() + "," + dict.getLanguageCode() + ")";
            if (hashSet.contains(hash)) {
                sb.append("Duplicated dictionary definition: ").append(hash).append("\n");
                continue;
            }
            hashSet.add(hash);
            Map<String, ProcessDBDictionaryItem> items = dict.getItems();
			for (ProcessDBDictionaryItem item : items.values()) {
                if (!Strings.hasText(item.getKey())) {
                    sb.append(hash).append(": empty item key").append("\n");
                    continue;
                }
                if (item.getValues().isEmpty()) {
                    sb.append(hash).append(": empty values set for key: ").append(item.getKey()).append("\n");
                    continue;
                }
                for (ProcessDBDictionaryItemValue val : item.getValues()) {
                    if (!Strings.hasText(val.getValue())) {
                        sb.append(hash).append(": empty value for key: ").append(item.getKey()).append("\n");
                        continue;
                    }
                    Date startDate = val.getValidStartDate();
                    Date endDate = val.getValidEndDate();
                    if (endDate != null && startDate != null && endDate.before(startDate)) {
                        sb.append(hash).append(": wrong date ranges in: ").append(val.getValue()).append(" for key: ").append(item.getKey()).append("\n");
                    }
                }
                StringBuilder dateSb = new StringBuilder();
                boolean startDateFullRange = false, endDateFullRange = false;
                for (ProcessDBDictionaryItemValue val : item.getValues()) {
                    startDateFullRange = validateSingleDate(dateSb, hash, item, startDateFullRange, val, val.getValidStartDate());
                    endDateFullRange = validateSingleDate(dateSb, hash, item, endDateFullRange, val, val.getValidEndDate());
                    if (!dateSb.toString().isEmpty()) {
                        break;
                    }
                }
                sb.append(dateSb);
            }
        }
        if (sb.toString().length() > 0) {
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
