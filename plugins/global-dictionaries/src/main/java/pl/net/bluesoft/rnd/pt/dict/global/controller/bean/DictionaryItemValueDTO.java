package pl.net.bluesoft.rnd.pt.dict.global.controller.bean;

import org.apache.commons.lang3.StringEscapeUtils;
import pl.net.bluesoft.rnd.processtool.dict.DictionaryItem;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryI18N;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemValue;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.FormatUtil;

import java.util.*;

/**
 * Created by pkuciapski on 2014-06-02.
 */
public class DictionaryItemValueDTO {
    private Long id;
    private String value;
    private String dateFrom;
    private String dateTo;
    private Collection<DictionaryItemExtDTO> extensions = new ArrayList<DictionaryItemExtDTO>();
    private Map<String, DictionaryI18NDTO> localizedValues = new HashMap<String, DictionaryI18NDTO>();

    private Boolean toDelete = Boolean.FALSE;
    private String selectedLanguage = "default";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public Collection<DictionaryItemExtDTO> getExtensions() {
        return extensions;
    }

    public void setExtensions(Collection<DictionaryItemExtDTO> extensions) {
        this.extensions = extensions;
    }

    public static DictionaryItemValueDTO createFrom(ProcessDBDictionaryItemValue value, I18NSource messageSource) {
        DictionaryItemValueDTO dto = new DictionaryItemValueDTO();
        dto.setId(value.getId());
        dto.setValue(StringEscapeUtils.escapeHtml4(value.getDefaultValue()));
        dto.setDateFrom(FormatUtil.formatShortDate(value.getValidFrom()));
        dto.setDateTo(FormatUtil.formatShortDate(value.getValidTo()));
        for (ProcessDBDictionaryI18N i18n: value.getLocalizedValues()) {
            DictionaryI18NDTO i18NDTO = DictionaryI18NDTO.createFrom(i18n, messageSource);
            dto.getLocalizedValues().put(i18NDTO.getLanguageCode(), i18NDTO);
        }
        for (ProcessDBDictionaryItemExtension ext : value.getExtensions()) {
            DictionaryItemExtDTO extDTO = DictionaryItemExtDTO.createFrom(ext, messageSource);
            dto.getExtensions().add(extDTO);
        }
        return dto;
    }

    public ProcessDBDictionaryItemValue toProcessDBDictionaryItemValue(String languageCode) {
        final ProcessDBDictionaryItemValue value = new ProcessDBDictionaryItemValue();
        if (this.getId() != null && !"".equals(this.getId()))
            value.setId(Long.valueOf(this.getId()));
        updateValue(value, languageCode);
        return value;
    }

    public void updateValue(ProcessDBDictionaryItemValue value, String languageCode) {
        value.setDefaultValue(StringEscapeUtils.unescapeHtml4(this.getValue()));
        if (this.getDateFrom() != null && !"".equals(this.getDateFrom()))
            value.setValidFrom(FormatUtil.parseDate("yyyy-MM-dd", this.getDateFrom()));
        if (this.getDateTo() != null && !"".equals(this.getDateTo())) {
            value.setValidTo(FormatUtil.parseDate("yyyy-MM-dd", this.getDateTo()));
        }
        for (DictionaryI18NDTO i18nDTO: this.getLocalizedValues().values()) {
            ProcessDBDictionaryI18N i18n = null;
            if (i18nDTO.getId() != null)
                i18n = getI18NById(value, i18nDTO.getId());
            if (i18n == null)
                value.getLocalizedValues().add(i18nDTO.toProcessDBDictionaryI18N(languageCode));
            else
                i18nDTO.update(i18n, languageCode);
        }
        for (DictionaryItemExtDTO extDTO : this.getExtensions()) {
            ProcessDBDictionaryItemExtension extension = null;
            if (extDTO.getId() != null)
                extension = getExtensionById(value, extDTO.getId());
            if (extension == null)
                value.addExtension(extDTO.toProcessDBDictionaryItemExtension(languageCode));
            else if (extDTO.getToDelete()) {
                value.getExtensions().remove(extension);
                extension.setItemValue(null);
            } else
                extDTO.updateExtension(extension, languageCode);
        }
    }

    private ProcessDBDictionaryI18N getI18NById(ProcessDBDictionaryItemValue value, Long id) {
        for (ProcessDBDictionaryI18N i18n : value.getLocalizedValues()) {
            if (i18n.getId() != null && i18n.getId().equals(id))
                return i18n;
        }
        return null;
    }

    private ProcessDBDictionaryItemExtension getExtensionById(ProcessDBDictionaryItemValue value, Long id) {
        for (ProcessDBDictionaryItemExtension extension : value.getExtensions()) {
            if (extension.getId() != null && extension.getId().equals(id))
                return extension;
        }
        return null;
    }

    public Boolean getToDelete() {
        return toDelete;
    }

    public void setToDelete(Boolean toDelete) {
        this.toDelete = toDelete;
    }

    public Map<String, DictionaryI18NDTO> getLocalizedValues() {
        return localizedValues;
    }

    public void setLocalizedValues(Map<String, DictionaryI18NDTO> localizedValues) {
        this.localizedValues = localizedValues;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }
}
