package org.aperteworkflow.files.dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;
import org.aperteworkflow.files.model.FileItemContent;

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
        String filePath = prepareStoragePath(relativeFilePath);
        File file = new File(filePath);
        OutputStream outputStream =
                FileUtils.openOutputStream(file);
        IOUtils.copyLarge(inputStream, outputStream);
        outputStream.flush();
        outputStream.close();
        return file;
    }

    @Override
    public void deleteFileFromStorage(File relativeFilePath) throws IOException {
        String filePath = config.getStorageRootDirPath() + File.separator + relativeFilePath;
        FileUtils.forceDelete(new File(filePath));
    }

    @Override
    public FileItemContent loadFileFromStorage(String relativeFilePath) throws IOException {
        String filePath = prepareStoragePath(relativeFilePath);
        File file = new File(filePath);
        FileItemContent content = new FileItemContent();
        content.setName(file.getName());
        content.setBytes(IOUtils.toByteArray(new FileInputStream(file)));
        return content;
    }

    private String prepareStoragePath(String relativeFilePath) {
        return config.getStorageRootDirPath() + File.separator + relativeFilePath;
    }

    @Override
    public String getRelativeFilePath(File file) {
            String storageRootPath = config.getStorageRootDirPath();
            return file.getAbsolutePath().replace(storageRootPath, "");
    }

}
