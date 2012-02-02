package org.aperteworkflow.editor.domain;

import java.io.Serializable;

public class ProcessModelConfig implements Serializable {
    
    private String modelerRepoDirectory;
    private String fileName;
    private String directory;
    private Boolean newModel;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Boolean isNewModel() {
        return newModel;
    }

    public void setNewModel(Boolean newModel) {
        this.newModel = newModel;
    }

    public String getModelerRepoDirectory() {
        return modelerRepoDirectory;
    }

    public void setModelerRepoDirectory(String modelerRepoDirectory) {
        this.modelerRepoDirectory = modelerRepoDirectory;
    }
}
