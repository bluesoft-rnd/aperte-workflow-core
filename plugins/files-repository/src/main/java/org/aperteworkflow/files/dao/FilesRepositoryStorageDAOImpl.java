package org.aperteworkflow.files.dao;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.aperteworkflow.files.model.FileItemContent;

import java.io.*;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryStorageDAOImpl implements FilesRepositoryStorageDAO {

    private String rootPath;

    public FilesRepositoryStorageDAOImpl(String rootPath) {
        this.rootPath = rootPath;
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
        IOUtils.closeQuietly(outputStream);
        return file;
    }

    @Override
    public void deleteFileFromStorage(File relativeFilePath) throws IOException {
        String filePath = rootPath + File.separator + relativeFilePath;
        FileUtils.forceDelete(new File(filePath));
    }

    @Override
    public FileItemContent loadFileFromStorage(String relativeFilePath) throws IOException {
        String filePath = prepareStoragePath(relativeFilePath);
        File file = new File(filePath);
        FileItemContent content = new FileItemContent();
        content.setName(file.getName());
        InputStream inputStream = new FileInputStream(file);
        content.setBytes(IOUtils.toByteArray(inputStream));
        IOUtils.closeQuietly(inputStream);
        return content;
    }

    private String prepareStoragePath(String relativeFilePath) {
        return rootPath + File.separator + relativeFilePath;
    }

    @Override
    public String getRelativeFilePath(File file) {
            return file.getAbsolutePath().replace(rootPath, "");
    }

}
