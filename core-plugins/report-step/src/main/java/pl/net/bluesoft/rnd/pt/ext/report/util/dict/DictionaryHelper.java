package pl.net.bluesoft.rnd.pt.ext.report.util.dict;

/**
 * Utility class for accessing localized dictionaries in reports.
 */
public interface DictionaryHelper {
    public String getDictionaryValue(String dictionaryName, String languageCode, String key);
}
