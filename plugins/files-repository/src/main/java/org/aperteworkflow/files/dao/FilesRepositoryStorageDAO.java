package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface FilesRepositoryStorageDAO {
    File uploadFileToStorage(InputStream inputStream, String fileName) throws IOException;
    void deleteFileFromStorage(File file) throws IOException;

    File loadFileFromStorage(String relativeFilePath);

    String getRelativeFilePath(File file);
}
