package pl.net.bluesoft.rnd.pt.dict.global.controller.bean;

import org.apache.commons.lang3.StringEscapeUtils;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Created by pkuciapski on 2014-06-02.
 */
public class DictionaryItemExtDTO {
    private Long id;
    private String key;
    private String value;
    private Boolean toDelete = Boolean.FALSE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static DictionaryItemExtDTO createFrom(ProcessDBDictionaryItemExtension ext, I18NSource messageSource) {
        final DictionaryItemExtDTO dto = new DictionaryItemExtDTO();
        dto.setId(ext.getId());
        dto.setKey(StringEscapeUtils.escapeHtml4(ext.getName()));
        dto.setValue(StringEscapeUtils.escapeHtml4(ext.getValue()));
        return dto;
    }

    public Boolean getToDelete() {
        return toDelete;
    }

    public void setToDelete(Boolean toDelete) {
        this.toDelete = toDelete;
    }

    public ProcessDBDictionaryItemExtension toProcessDBDictionaryItemExtension(String languageCode) {
        final ProcessDBDictionaryItemExtension extension = new ProcessDBDictionaryItemExtension();
        updateExtension(extension, languageCode);
        return extension;
    }

    public void updateExtension(ProcessDBDictionaryItemExtension extension, String languageCode) {
        if (this.getId() != null)
            extension.setId(this.getId());
        extension.setValue(StringEscapeUtils.unescapeHtml4(this.getValue()));
        extension.setName(StringEscapeUtils.unescapeHtml4(this.getKey()));
    }
}
