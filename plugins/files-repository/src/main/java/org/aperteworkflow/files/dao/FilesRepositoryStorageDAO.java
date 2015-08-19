package org.aperteworkflow.files.dao;

import org.aperteworkflow.files.model.FileItemContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface FilesRepositoryStorageDAO {
    File uploadFileToStorage(InputStream inputStream, String fileName) throws IOException;
    void deleteFileFromStorage(File file) throws IOException;
    FileItemContent loadFileFromStorage(String relativeFilePath) throws IOException;
    String getRelativeFilePath(File file);
}
