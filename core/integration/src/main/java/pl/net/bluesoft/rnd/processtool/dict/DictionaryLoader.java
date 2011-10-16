package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.dict.xml.Dictionary;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntry;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntryExtension;
import pl.net.bluesoft.rnd.processtool.dict.xml.ProcessDictionaries;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.pt.utils.xml.OXHelper;
import pl.net.bluesoft.util.lang.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DictionaryLoader extends OXHelper {
    private static DictionaryLoader instance = new DictionaryLoader();

    public static DictionaryLoader getInstance() {
        return instance;
    }

    @Override
    protected Class[] getSupportedClasses() {
        return new Class[] {
                Dictionary.class,
                DictionaryEntry.class,
                DictionaryEntryExtension.class,
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
            for (DictionaryEntry entry : dict.getEntries()) {
                ProcessDBDictionaryItem dbItem = new ProcessDBDictionaryItem();
                dbItem.setDescription(entry.getDescription());
                dbItem.setKey(entry.getKey());
                dbItem.setValue(entry.getValue());
                dbItem.setValueType(entry.getValueType());
                for (DictionaryEntryExtension ext : entry.getExtensions()) {
                    ProcessDBDictionaryItemExtension dbItemExt = new ProcessDBDictionaryItemExtension();
                    dbItemExt.setName(ext.getName());
                    dbItemExt.setValue(ext.getValue());
                    dbItemExt.setValueType(ext.getValueType());
                    dbItem.addItemExtension(dbItemExt);
                }
                dbDict.addItem(dbItem);
            }
            dbDict.setDefaultDictionary(StringUtil.hasText(processDictionaries.getDefaultLanguage())
                    && processDictionaries.getDefaultLanguage().equals(dbDict.getLanguageCode()));
            result.add(dbDict);
        }
        return result;
    }
}
