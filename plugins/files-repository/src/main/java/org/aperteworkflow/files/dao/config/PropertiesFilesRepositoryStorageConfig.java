package org.aperteworkflow.files.dao.config;

import java.util.Properties;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class PropertiesFilesRepositoryStorageConfig implements FilesRepositoryStorageConfig {

    public static final String FILESREPOSITORY_STORAGE_ROOTDIR_PATH_KEY = "filesrepository.storage.rootdir.path";
    private Properties properties;

    public PropertiesFilesRepositoryStorageConfig(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getStorageRootDirPath() {
        return getProperties().getProperty(FILESREPOSITORY_STORAGE_ROOTDIR_PATH_KEY);
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
