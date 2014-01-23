package org.aperteworkflow.files;

import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.FilesRepositoryItem;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface IFilesRepositoryFacade {
    FilesRepositoryItem uploadFile(InputStream inputStream, String contentType, Long processInstanceId, String fileName, String fileDescription, String creatorLogin) throws UploadFileException;

    void deleteFile(Long processInstanceId, Long filesRepositoryItemId) throws DeleteFileException;

    FileItemContent downloadFile(Long processInstanceId, Long fileId) throws DownloadFileException;

    Collection<FilesRepositoryItem> getFilesList(Long processInstanceId);

    void updateDescription(Long processInstanceId, Long filesRepositoryItemId, String fileDescription) throws UpdateDescriptionException;
}
