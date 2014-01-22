package org.aperteworkflow.files.dao.config;

import pl.net.bluesoft.rnd.processtool.IProcessToolSettings;
import pl.net.bluesoft.util.lang.Formats;

import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class PtSettingsFilesRepositoryStorageConfig implements FilesRepositoryStorageConfig {

    private static final Logger logger = Logger.getLogger(PtSettingsFilesRepositoryStorageConfig.class.getName());

    public static final String FILESREPOSITORY_STORAGE_ROOTDIR_PATH_KEY = "filesrepository.storage.rootdir.path";

    private IProcessToolSettings filesRepositoryStorageSetting;

    public PtSettingsFilesRepositoryStorageConfig() {
        filesRepositoryStorageSetting = new IProcessToolSettings() {
            @Override
            public String toString() {
                return FILESREPOSITORY_STORAGE_ROOTDIR_PATH_KEY;
            }
        };
    }

    @Override
    public String getStorageRootDirPath() {
        return getThreadProcessToolContext() != null ? getThreadProcessToolContext().getSetting(filesRepositoryStorageSetting) : null;
    }
}
