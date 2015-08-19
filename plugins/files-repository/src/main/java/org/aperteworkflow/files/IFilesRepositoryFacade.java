package org.aperteworkflow.files;

import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public interface IFilesRepositoryFacade {
    IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, IAttributesConsumer filesAttributeConsumer,
									String fileName, String fileDescription, String creatorLogin, FilesRepositoryAttributeFactory factory) throws UploadFileException;
	IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, IAttributesConsumer filesAttributeConsumer,
									String fileName, String fileDescription, String creatorLogin, FilesRepositoryAttributeFactory factory,
									Boolean sendWithMail) throws UploadFileException;
	IFilesRepositoryItem uploadFile(InputStream inputStream, String contentType, IAttributesConsumer filesAttributeConsumer,
									String fileName, String fileDescription, String creatorLogin, FilesRepositoryAttributeFactory factory,
									Boolean sendWithMail, String groupId) throws UploadFileException;

    void deleteFile(IAttributesProvider filesAttributeProvider, Long filesRepositoryItemId) throws DeleteFileException;

    FileItemContent downloadFile(Long fileItemId) throws DownloadFileException;

    Collection<? extends IFilesRepositoryItem> getFilesList(IAttributesProvider filesAttributeProvider);

	Collection<? extends IFilesRepositoryItem> copy(List<IFilesRepositoryItem> files, IAttributesConsumer filesConsumer, FilesRepositoryAttributeFactory factory);

	enum FileListFilter {
		ALL,
		WITHOUT_EMAIL_ATTACHMENTS,
		ONLY_EMAIL_ATTACHMENTS,
	}
    Collection<? extends IFilesRepositoryItem> getFilesList(IAttributesProvider filesAttributeProvider, FileListFilter filter);

	IFilesRepositoryItem getFileItem(Long id);

    void updateDescription(Long filesRepositoryItemId, String fileDescription) throws UpdateDescriptionException;

    void updateSendWithMail(Long filesRepositoryItemId, Boolean sendWithMail);

}
