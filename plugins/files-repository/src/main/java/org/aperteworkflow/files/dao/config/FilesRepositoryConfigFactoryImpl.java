package org.aperteworkflow.files.dao.config;

import org.aperteworkflow.files.IFilesRepositoryFacade;
import pl.net.bluesoft.util.lang.Classes;

import java.util.Properties;

/**
 * @author pwysocki@bluesoft.net.pl
 */

public class FilesRepositoryConfigFactoryImpl implements FilesRepositoryConfigFactory {
    public FilesRepositoryStorageConfig createFilesRepositoryStorageConfig() {
        FilesRepositoryStorageConfig storageConfig =  new PtSettingsFilesRepositoryStorageConfig();
        if (storageConfig != null && storageConfig.getStorageRootDirPath() == null || storageConfig.getStorageRootDirPath().length() == 0) {
            Properties properties = Classes.loadProperties(IFilesRepositoryFacade.class, "plugin.properties");
            storageConfig = new PropertiesFilesRepositoryStorageConfig(properties);
        }
        if (storageConfig != null && storageConfig.getStorageRootDirPath() == null || storageConfig.getStorageRootDirPath().length() == 0) {
            throw new RuntimeException("Storage root directory not defined either in db or plugin.propeties file!");
        }
        return storageConfig;
    }
}
