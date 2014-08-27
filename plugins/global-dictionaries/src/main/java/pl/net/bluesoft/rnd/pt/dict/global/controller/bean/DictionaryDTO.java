package pl.net.bluesoft.rnd.pt.dict.global.controller.bean;

import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Created by pkuciapski on 2014-06-02.
 */
public class DictionaryDTO {
    private String id;
    private String name;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static DictionaryDTO createFrom(ProcessDBDictionary dict, I18NSource messageSource) {
        DictionaryDTO dto = new DictionaryDTO();
        dto.setId(dict.getDictionaryId());
        dto.setName(dict.getName(messageSource.getLocale()));
        if (dto.getName() == null || "".equals(dto.getName())) {
            dto.setName(dict.getDefaultName());
        }
        return dto;
    }
}
