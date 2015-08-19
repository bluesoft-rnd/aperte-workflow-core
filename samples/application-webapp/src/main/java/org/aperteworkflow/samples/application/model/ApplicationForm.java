package org.aperteworkflow.samples.application.model;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * Created by Dominik Dębowczyk on 2015-07-31.
 */
public class ApplicationForm {

    private String name;
    private String surname;
    private String description;
    private CommonsMultipartFile attachmentFile;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CommonsMultipartFile getAttachmentFile() {
        return attachmentFile;
    }

    public void setAttachmentFile(CommonsMultipartFile attachmentFile) {
        this.attachmentFile = attachmentFile;
    }
}
