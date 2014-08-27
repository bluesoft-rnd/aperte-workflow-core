package pl.net.bluesoft.rnd.pt.dict.global.controller.bean;

import org.apache.commons.lang3.StringEscapeUtils;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryI18N;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pkuciapski on 2014-05-30.
 */
public class DictionaryItemDTO {
    private Long id;
    private String key;
    private String description;

    private Collection<DictionaryItemValueDTO> values = new ArrayList<DictionaryItemValueDTO>();

    private Map<String, DictionaryI18NDTO> localizedDescriptions = new HashMap<String, DictionaryI18NDTO>();

    private String selectedLanguage = "default";

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, DictionaryI18NDTO> getLocalizedDescriptions() {
        return localizedDescriptions;
    }

    public void setLocalizedDescriptions(Map<String, DictionaryI18NDTO> localizedDescriptions) {
        this.localizedDescriptions = localizedDescriptions;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public ProcessDBDictionaryItem toProcessDBDictionaryItem(final String languageCode) {
        ProcessDBDictionaryItem item = new ProcessDBDictionaryItem();
        if (this.getId() != null)
            item.setId(this.getId());
        updateItem(item, languageCode);
        return item;
    }

    public void updateItem(ProcessDBDictionaryItem item, String languageCode) {
        item.setKey(this.getKey());
        final DictionaryI18NDTO defaultI18N = DictionaryI18NDTO.getDefaultI18N(this.getLocalizedDescriptions());
        if (defaultI18N != null && defaultI18N.getText() != null)
            item.setDefaultDescription(StringEscapeUtils.unescapeHtml4(defaultI18N.getText()));
        else
            item.setDefaultDescription(StringEscapeUtils.unescapeHtml4(this.getDescription()));
        for (DictionaryI18NDTO i18NDTO : this.getLocalizedDescriptions().values()) {
            ProcessDBDictionaryI18N i18n = null;
            if (i18NDTO.getId() != null)
                i18n = getI18NById(item, i18NDTO.getId());
            if (i18n == null)
                item.getLocalizedDescriptions().add(i18NDTO.toProcessDBDictionaryI18N(languageCode));
            else
                i18NDTO.update(i18n, languageCode);
        }
        for (DictionaryItemValueDTO valueDTO : this.getValues()) {
            ProcessDBDictionaryItemValue value = null;
            if (valueDTO.getId() != null)
                value = getValueById(item, valueDTO.getId());
            if (value == null)
                item.addValue(valueDTO.toProcessDBDictionaryItemValue(languageCode));
            else if (valueDTO.getToDelete())
                item.removeValue(value);
            else
                valueDTO.updateValue(value, languageCode);
        }
    }

    private ProcessDBDictionaryI18N getI18NById(ProcessDBDictionaryItem item, Long id) {
        for (ProcessDBDictionaryI18N i18n : item.getLocalizedDescriptions()) {
            if (i18n.getId() != null && i18n.getId().equals(id))
                return i18n;
        }
        return null;
    }

    private ProcessDBDictionaryItemValue getValueById(ProcessDBDictionaryItem item, Long id) {
        for (ProcessDBDictionaryItemValue value : item.getValues()) {
            if (value.getId() != null && value.getId().equals(id))
                return value;
        }
        return null;
    }

    public Collection<DictionaryItemValueDTO> getValues() {
        return values;
    }

    public void setValues(Collection<DictionaryItemValueDTO> values) {
        this.values = values;
    }

    public static DictionaryItemDTO createFrom(ProcessDBDictionaryItem item, I18NSource messageSource) {
        DictionaryItemDTO dto = new DictionaryItemDTO();
        dto.setId(item.getId());
        dto.setKey(item.getKey());
        dto.setDescription(StringEscapeUtils.escapeHtml4(item.getDefaultDescription()));
        for (ProcessDBDictionaryI18N desc : item.getLocalizedDescriptions()) {
            DictionaryI18NDTO i18NDTO = DictionaryI18NDTO.createFrom(desc, messageSource);
            dto.getLocalizedDescriptions().put(i18NDTO.getLanguageCode(), i18NDTO);
        }
        for (ProcessDBDictionaryItemValue value : item.getValues()) {
            DictionaryItemValueDTO valueDTO = DictionaryItemValueDTO.createFrom(value, messageSource);
            dto.getValues().add(valueDTO);
        }
        return dto;
    }
}
