package org.aperteworkflow.files;

import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.IFilesRepositoryItem;

import java.io.InputStream;
import java.util.Collection;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface IFilesRepositoryFacade {
    IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, Long parentObjectId, String fileName, String fileDescription, String creatorLogin) throws UploadFileException;

    void deleteFile(Long parentObjectId, Long filesRepositoryItemId) throws DeleteFileException;

    FileItemContent downloadFile(Long parentObjectId, Long fileId) throws DownloadFileException;

    Collection<? extends IFilesRepositoryItem> getFilesList(Long parentObjectId);

    void updateDescription(Long parentObjectId, Long filesRepositoryItemId, String fileDescription) throws UpdateDescriptionException;
}
