package pl.net.bluesoft.lot.casemanagement.model;

import pl.net.bluesoft.util.lang.Formats;

/**
 * Created by pkuciapski on 2014-04-24.
 */
public class CaseDTO {
    private long id;
    private String name;
    private String number;
    private String definitionName;
    private String currentStageName;
    private String createDate;
    private String modificationDate;

    public CaseDTO(final Case caseInstance) {
        setId(caseInstance.getId());
        setName(caseInstance.getName());
        setNumber(caseInstance.getNumber());
        setDefinitionName(caseInstance.getDefinition().getName());
        setCurrentStageName(caseInstance.getCurrentStage().getName());
        setCreateDate(Formats.formatFullDate(caseInstance.getCreateDate()));
        setModificationDate(Formats.formatFullDate(caseInstance.getModificationDate()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public String getCurrentStageName() {
        return currentStageName;
    }

    public void setCurrentStageName(String currentStageName) {
        this.currentStageName = currentStageName;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
