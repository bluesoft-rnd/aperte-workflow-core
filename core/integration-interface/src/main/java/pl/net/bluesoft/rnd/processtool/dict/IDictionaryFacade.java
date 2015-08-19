package pl.net.bluesoft.rnd.processtool.dict;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * Dictionary facade to help operating with dictionaries.
 *
 * @author: mpawlak@bluesoft.net.pl
 */
public interface IDictionaryFacade
{
    /** Get all dictionary items by given dict name */
    Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale);

    /** Get all dictionary name with value extension filtered by given filter: type=test;type2=anothertest
     * Only items with extenstion meeting filter conditions will be returned
     */
    Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale, String filter);

    Collection<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale, String filter, Date date);

    /*
        dictionaryName: dictionary name (key)
        locale: current locale, which must be given to determinate value name translation
        filter: use "{extension_key}={extension_value}" syntax to filter by extenstion using like, use "key={value}"
                to filter by item key, "value={value}" to filter by value. Filters are using contains (like) and
                are not case sensitive. Use null to not use filter
        date: if not provided, current date value will be used
        sortBy: "key" if sort by key, "value" if sort by value, extenstion key otherwise, use null to ignore sorting
     */
    List<DictionaryItem> getAllDictionaryItems(String dictionaryName, Locale locale, String filter, Date date, String sortBy);

    DictionaryItem getDictionaryItem(String dictionaryName, String key, Locale locale);

    DictionaryItem getDictionaryItemForDate(String dictionaryName, String key, Locale locale, Date date);

    Collection<DictionaryItem> getFlatDictionaryItemsList(String dictionaryName, Locale locale, String filter);
}
