package org.aperteworkflow.files;

import org.aperteworkflow.files.dao.FilesRepositoryItemDAO;
import org.aperteworkflow.files.dao.FilesRepositoryItemDAOImpl;
import org.aperteworkflow.files.dao.FilesRepositoryStorageDAO;
import org.aperteworkflow.files.dao.FilesRepositoryStorageDAOImpl;
import org.aperteworkflow.files.dao.config.FilesRepositoryConfigFactory;
import org.aperteworkflow.files.dao.config.FilesRepositoryConfigFactoryImpl;
import org.aperteworkflow.files.dao.config.FilesRepositoryStorageConfig;
import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.hibernate.Session;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;

import java.io.*;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FilesRepositoryFacade implements IFilesRepositoryFacade {

    private static Logger logger = Logger.getLogger(FilesRepositoryFacade.class.getName());
    private ProcessInstanceDAO customProcessInstanceDAO;

    private FilesRepositoryStorageConfig storageConfig;

    private Session customSession;

    public FilesRepositoryFacade() {
        this(null, new FilesRepositoryConfigFactoryImpl(), null);
    }


    public FilesRepositoryFacade(Session customSession, FilesRepositoryConfigFactory configFactory, ProcessInstanceDAO customProcessInstanceDAO) {
        this.customSession = customSession;
        this.storageConfig = configFactory.createFilesRepositoryStorageConfig();
        this.customProcessInstanceDAO = customProcessInstanceDAO;
    }

    private FilesRepositoryItemDAO getFilesRepositoryItemDAO() {
        Session sessionToUse = customSession != null ? customSession : ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
        ProcessInstanceDAO piDaoToUse = customProcessInstanceDAO != null ? customProcessInstanceDAO : ProcessToolContext.Util.getThreadProcessToolContext().getProcessInstanceDAO();
        return new FilesRepositoryItemDAOImpl(sessionToUse, piDaoToUse);
    }

    private FilesRepositoryStorageDAO getFilesRepositoryStorageDAO() {
        return new FilesRepositoryStorageDAOImpl(storageConfig);
    }

    @Override
    public Long uploadFile(InputStream inputStream, Long processInstanceId, String fileName, String fileDescription, String creatorLogin) throws UploadFileException {
        Long result;
        File file;
        try {
            file = getFilesRepositoryStorageDAO().uploadFileToStorage(inputStream, fileName);
        } catch (IOException e) {
            throw new UploadFileException("Cannot write file to storage", e);
        }
        String fileRelativePath = getFilesRepositoryStorageDAO().getRelativeFilePath(file);
        result = getFilesRepositoryItemDAO().addItem(processInstanceId, fileName, fileDescription, fileRelativePath, creatorLogin).getId();
        return result;
    }

    @Override
    public void deleteFile(Long processInstanceId, Long filesRepositoryItemId) throws DeleteFileException {
        FilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(filesRepositoryItemId);
        if (filesRepositoryItem == null) {
            throw new DeleteFileException("File item with id=[" + filesRepositoryItemId + "] not found.");
        }
        if (filesRepositoryItem.getProcessInstance() == null || !filesRepositoryItem.getProcessInstance().getId().equals(processInstanceId)) {
            throw new DeleteFileException("File from repository. File item is not connected to processInstanceId=[" + processInstanceId + "].");
        }
        getFilesRepositoryItemDAO().deleteById(filesRepositoryItemId);
        try {
            getFilesRepositoryStorageDAO().deleteFileFromStorage(new File(filesRepositoryItem.getRelativePath()));
        } catch (IOException e) {
            throw new DeleteFileException("File from path=[" + filesRepositoryItem.getRelativePath() + "].", e);
        }
    }

    @Override
    public OutputStream downloadFile(Long processInstanceId, Long fileRepositoryItemId) throws DownloadFileException {
        FilesRepositoryItem filesRepositoryItem = getFilesRepositoryItemDAO().getItemById(fileRepositoryItemId);
        if (filesRepositoryItem == null) {
            throw new DownloadFileException("File item with id=[" + fileRepositoryItemId + "] not found.");
        }
        if (filesRepositoryItem.getProcessInstance() == null || !filesRepositoryItem.getProcessInstance().getId().equals(processInstanceId)) {
            throw new DownloadFileException("File item is not connected to processInstanceId=[" + processInstanceId + "].");
        }
        File file = getFilesRepositoryStorageDAO().loadFileFromStorage(filesRepositoryItem.getRelativePath());
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new DownloadFileException("File not found in storage.", e);
        }
    }

    @Override
    public Collection<FilesRepositoryItem> getFilesList(Long processInstanceId) {
        return getFilesRepositoryItemDAO().getItemsFor(processInstanceId);
    }

}
