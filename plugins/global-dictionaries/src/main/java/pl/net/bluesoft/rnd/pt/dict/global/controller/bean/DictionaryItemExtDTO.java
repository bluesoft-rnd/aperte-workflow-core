package pl.net.bluesoft.rnd.pt.dict.global.controller.bean;

import org.apache.commons.lang3.StringEscapeUtils;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionaryItemExtension;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Created by pkuciapski on 2014-06-02.
 */
public class DictionaryItemExtDTO {
    private Long id;
    private String key;
    private String value;
	private String description;
    private Boolean toDelete = Boolean.FALSE;
	private boolean default_;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDefault_() {
		return default_;
	}

	public void setDefault_(boolean default_) {
		this.default_ = default_;
	}

	public static DictionaryItemExtDTO createFrom(ProcessDBDictionaryItemExtension ext, I18NSource messageSource) {
        final DictionaryItemExtDTO dto = new DictionaryItemExtDTO();
        dto.setId(ext.getId());
        dto.setKey(StringEscapeUtils.escapeHtml4(ext.getName()));
        dto.setValue(StringEscapeUtils.escapeHtml4(ext.getValue()));
		if (hasText(ext.getDescription())) {
			dto.setDescription(StringEscapeUtils.escapeHtml4(ext.getDescription()));
		}
		dto.setDefault_(ext.getDefault_());
        return dto;
    }

    public Boolean getToDelete() {
        return toDelete;
    }

    public void setToDelete(Boolean toDelete) {
        this.toDelete = toDelete;
    }

    public ProcessDBDictionaryItemExtension toProcessDBDictionaryItemExtension(ProcessDBDictionary dictionary, String languageCode) {
        final ProcessDBDictionaryItemExtension extension = new ProcessDBDictionaryItemExtension();
        updateExtension(dictionary, extension, languageCode);
        return extension;
    }

    public void updateExtension(ProcessDBDictionary dictionary, ProcessDBDictionaryItemExtension extension, String languageCode) {
        if (this.getId() != null)
            extension.setId(this.getId());
        extension.setValue(StringEscapeUtils.unescapeHtml4(this.getValue()));
        extension.setName(StringEscapeUtils.unescapeHtml4(this.getKey()));

        extension.setDefault_(dictionary.isDefaultExtenstion(extension.getName()));

    }
}
