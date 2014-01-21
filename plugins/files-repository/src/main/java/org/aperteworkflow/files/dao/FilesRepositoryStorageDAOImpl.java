package org.aperteworkflow.files.dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;

import java.io.*;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryStorageDAOImpl implements FilesRepositoryStorageDAO {

    private FilesRepositoryStorageConfig config;

    public FilesRepositoryStorageDAOImpl(FilesRepositoryStorageConfig config) {
        this.config = config;
    }

    @Override
    public File uploadFileToStorage(InputStream inputStream, String relativeFilePath) throws IOException {
        return saveInputStreamToFile(inputStream, relativeFilePath);
    }

    private File saveInputStreamToFile(InputStream inputStream, String relativeFilePath) throws IOException {
        String filePath = config.getStorageRootDirPath() + File.separator + relativeFilePath;
        File file = new File(filePath);
        OutputStream outputStream =
                new FileOutputStream(file);
        IOUtils.copyLarge(inputStream, outputStream);
        return file;
    }

    @Override
    public void deleteFileFromStorage(File relativeFilePath) throws IOException {
        String filePath = config.getStorageRootDirPath() + File.separator + relativeFilePath;
        FileUtils.forceDelete(new File(filePath));
    }

    @Override
    public File loadFileFromStorage(String relativeFilePath) {
        String filePath = config.getStorageRootDirPath() + File.separator + relativeFilePath;
        return new File(filePath);
    }

     @Override
    public String getRelativeFilePath(File file) {
            String storageRootPath = config.getStorageRootDirPath();
            return file.getAbsolutePath().replace(storageRootPath, "");
    }

}
