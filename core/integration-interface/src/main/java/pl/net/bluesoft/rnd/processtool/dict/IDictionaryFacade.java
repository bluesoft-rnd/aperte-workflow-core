package pl.net.bluesoft.rnd.processtool.dict;

import java.util.Collection;
import java.util.Locale;
import java.util.Date;

/**
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
}
