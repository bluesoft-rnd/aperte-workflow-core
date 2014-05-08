package org.aperteworkflow.admin.controller;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.web.domain.AbstractResultBean;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Date;
import java.util.Set;

public class ProcessInstanceBean extends AbstractResultBean {

    private String definitionName;
    private String creatorLogin;
    private Date creationDate;
    private String status;
    private String externalKey;
    private String internalId;
    private ProcessDefinitionConfig definition;
    private Set<ProcessInstanceAttribute> processAttributes;

    public static ProcessInstanceBean createFrom(ProcessInstance instance, I18NSource messageSource) {
        ProcessInstanceBean processInstanceBean = new ProcessInstanceBean();

        processInstanceBean.definitionName = instance.getDefinitionName();
        processInstanceBean.creatorLogin = instance.getCreatorLogin();
        processInstanceBean.creationDate = instance.getCreateDate();
        processInstanceBean.status = instance.getBusinessStatus();
        processInstanceBean.internalId = instance.getInternalId();
        processInstanceBean.externalKey = instance.getExternalKey();
        processInstanceBean.definition = instance.getDefinition();
        processInstanceBean.processAttributes = instance.getProcessAttributes();

        return processInstanceBean;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public void setDefinitionNameefinitionName(String definitionName) {
        this.definitionName = definitionName;
    }

    public String getCreatorLogin() {
        return creatorLogin;
    }

    public void setCreatorLogin(String creatorLogin) {
        this.creatorLogin = creatorLogin;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }
}
