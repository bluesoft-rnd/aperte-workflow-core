package org.aperteworkflow.files.model;

/**
 * Created by pkuciapski on 2014-05-12.
 */
public enum FilesRepositoryAttributes {
    FILES("files");

    private final String value;

    FilesRepositoryAttributes(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}

