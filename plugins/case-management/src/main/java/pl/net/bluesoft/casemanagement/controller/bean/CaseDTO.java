package pl.net.bluesoft.casemanagement.controller.bean;

import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.util.CaseProcessUtil;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.Formats;

/**
 * Created by pkuciapski on 2014-04-24.
 */
public class CaseDTO implements Comparable<CaseDTO> {
    private static final String CASE_DEFINITION_NAME_PREFIX = "case.definition.name.";
    private static final String CASE_STATE_DEFINITION_NAME_PREFIX = "case.state.definition.name.";
    private long id;
    private String name;
    private String number;
    private String definitionName;
    private String currentStageName;
    private String createDate;
    private String modificationDate;
    private String caseStateProcessesJson;

    public static CaseDTO createFrom(Case caseInstance, I18NSource messageSource) {
        CaseDTO caseDTO = new CaseDTO();
        caseDTO.setId(caseInstance.getId());
        caseDTO.setName(caseInstance.getName());
        caseDTO.setNumber(caseInstance.getNumber());
        caseDTO.setDefinitionName(messageSource.getMessage(CASE_DEFINITION_NAME_PREFIX + caseInstance.getDefinition().getName()));
        if (caseInstance.getCurrentStage() != null) {
            caseDTO.setCurrentStageName(messageSource.getMessage(CASE_STATE_DEFINITION_NAME_PREFIX + caseInstance.getDefinition().getName()
                    + "." + caseInstance.getCurrentStage().getName()));
            caseDTO.setCaseStateProcessesJson(CaseProcessUtil.toJson(caseInstance.getCurrentStage().getCaseStateDefinition().getProcesses(), messageSource));
        }
        caseDTO.setCreateDate(Formats.formatFullDate(caseInstance.getCreateDate()));
        caseDTO.setModificationDate(Formats.formatFullDate(caseInstance.getModificationDate()));


        return caseDTO;
    }

    public static String getCasePropertyName(final String dtoPropertyName) {
        if ("definitionName".equals(dtoPropertyName)) {
            return "definition.name";
        }
        if ("modificationDate".equals(dtoPropertyName))
            return "coalesce(modificationDate, createDate)";
        return dtoPropertyName;
    }

    @Override
    public int compareTo(CaseDTO caseDTO) {
        return this.getNumber().compareTo(caseDTO.getNumber());
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

    public String getCaseStateProcessesJson() {
        return caseStateProcessesJson;
    }

    public void setCaseStateProcessesJson(String caseStateProcessesJson) {
        this.caseStateProcessesJson = caseStateProcessesJson;
    }
}
