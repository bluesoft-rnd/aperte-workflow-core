package org.aperteworkflow.files.controller;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import org.aperteworkflow.files.exceptions.DeleteFileException;
import org.aperteworkflow.files.exceptions.DownloadFileException;
import org.aperteworkflow.files.exceptions.UpdateDescriptionException;
import org.aperteworkflow.files.model.FileItemContent;
import org.aperteworkflow.files.model.FilesRepositoryItemDTO;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.usersource.IPortalUserSource;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.Strings.hasText;

/**
 * Created by pkuciapski on 2014-05-13.
 */
public abstract class AbstractFilesController implements IOsgiWebController {
    public static final String FILE_DESCRIPTION_PARAM_NAME = "fileDescription";
    public static final String FILE_SENDWITHMAIL_PARAM_NAME = "fileSendWithMail";
    private static Logger logger = Logger.getLogger(FilesController.class.getName());

    private static final String PROCESS_INSTANCE_ID_REQ_PARAM_NAME = "processInstanceId";
    private static final String FILES_REPOSITORY_ITEM_ID_REQ_PARAM_NAME = "filesRepositoryItemId";

    @Autowired
    protected IFilesRepositoryFacade filesRepoFacade;

    @Autowired
    protected IPortalUserSource portalUserSource;

    protected abstract IAttributesConsumer getAttributesConsumer(Long id);
    protected abstract IAttributesProvider getAttributesProvider(Long id);

    @ControllerMethod(action = "uploadFile")
    public GenericResultBean uploadFile(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();

        HttpServletRequest request = invocation.getRequest();
        try {
            //process only if its multipart content
            if (ServletFileUpload.isMultipartContent(request)) {
                // Create a new file upload handler
                ServletFileUpload upload = new ServletFileUpload();
                // Mandatory parameters for upload

                // Parse the request
                FileItemIterator iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    InputStream stream = item.openStream();
                    if (!item.isFormField()) {
                        InputStream fileInputStream = stream;
                        String contentType = item.getContentType();
                        Long processInstanceId = getProcessInstanceId(request);
                        String fileName = item.getName();
                        String fileDescription = null;
                        String creatorLogin = getCreatorLogin(request);
						String sendWithMailString = request.getParameter(FILE_SENDWITHMAIL_PARAM_NAME);
						Boolean sendWithMail = hasText(sendWithMailString) ? Boolean.valueOf(sendWithMailString) : null;

                        if (processInstanceId != null && fileName != null && fileName.length() > 0 && fileInputStream != null && creatorLogin != null && creatorLogin.length() > 0) {
                            IFilesRepositoryItem frItem = filesRepoFacade.uploadFile(
									fileInputStream, contentType, getAttributesConsumer(processInstanceId),
									fileName, fileDescription, creatorLogin, getAttributesFactory(),
									sendWithMail);
                            result.setData(new FilesRepositoryItemDTO(frItem));
                        } else {
                            logger.log(Level.WARNING, "[FILES_REPOSITORY] Not all parameters provided when calling filescontroller.uploadFile. All of [processInstanceId, fileName, fileInputStream, creatorLogin] are required.");
                        }
                        IOUtils.closeQuietly(fileInputStream);
                    }
                }
            } else {
                logger.log(Level.WARNING, "[FILES_REPOSITORY] For proper file processing request should be multipartcontent");
                throw new RuntimeException("[FILES_REPOSITORY] For proper file processing request should be multipartcontent");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot get file from request", e);
            result.addError("Cannot get file from request", e.getMessage());
        }
		return result;
    }

    protected abstract FilesRepositoryAttributeFactory getAttributesFactory();

    @ControllerMethod(action = "getFilesList")
    public GenericResultBean getFilesList(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        HttpServletRequest request = invocation.getRequest();
        Long processInstanceId = getProcessInstanceId(request);
        Collection<? extends IFilesRepositoryItem> fileRepoItems = filesRepoFacade.getFilesList(getAttributesProvider(processInstanceId));
        Collection<FilesRepositoryItemDTO> filesRepoItemsDTO = new ArrayList<FilesRepositoryItemDTO>();
        for (IFilesRepositoryItem frItem : fileRepoItems) {
            filesRepoItemsDTO.add(new FilesRepositoryItemDTO(frItem));
        }
        result.setData(filesRepoItemsDTO);
        return result;
    }

    @ControllerMethod(action = "deleteFile")
    public GenericResultBean deleteFile(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        HttpServletRequest request = invocation.getRequest();
        Long processInstanceId = getProcessInstanceId(request);
        Long filesRepositoryItemId = getFilesRepositoryItemId(request);
        try {
            IAttributesProvider provider = getAttributesProvider(processInstanceId);
            filesRepoFacade.deleteFile(provider, filesRepositoryItemId);
            Collection<? extends IFilesRepositoryItem> fileRepoItems = filesRepoFacade.getFilesList(provider);
            Collection<FilesRepositoryItemDTO> filesRepoItemsDTO = new ArrayList<FilesRepositoryItemDTO>();
            for (IFilesRepositoryItem frItem : fileRepoItems) {
                filesRepoItemsDTO.add(new FilesRepositoryItemDTO(frItem));
            }
            result.setData(filesRepoItemsDTO);
        } catch (DeleteFileException e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot delete requested file for repo item id=[" + filesRepositoryItemId + "]", e);
            result.addError("Cannot delete requested file for repo item id=[" + filesRepositoryItemId + "]", e.getMessage());
        }
        return result;
    }

    @ControllerMethod(action = "downloadFile")
    public GenericResultBean downloadFile(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        HttpServletRequest request = invocation.getRequest();
        Long processInstanceId = getProcessInstanceId(request);
        Long filesRepositoryItemId = getFilesRepositoryItemId(request);
        FileItemContent content = null;
        try {
            content = filesRepoFacade.downloadFile(filesRepositoryItemId);
        } catch (DownloadFileException e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot download requested file from repository for item id=[" + filesRepositoryItemId + "] and processInstanceId=[" + processInstanceId + "].", e);
            result.addError("Cannot download requested file from repository for item id=[" + filesRepositoryItemId + "] and processInstanceId=[" + processInstanceId + "].", e.getMessage());
            return result;
        }
        HttpServletResponse response = invocation.getResponse();
        try {
            sendInResponseOutputStream(request, response, content);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot download requested file from repository for item id=[" + filesRepositoryItemId + "] and processInstanceId=[" + processInstanceId + "].", e);
            result.addError("Cannot download requested file from repository for item id=[" + filesRepositoryItemId + "] and processInstanceId=[" + processInstanceId + "].", e.getMessage());
            return result;
        }
        return result;
    }

    @ControllerMethod(action = "updateDescription")
    public GenericResultBean updateDescription(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        HttpServletRequest request = invocation.getRequest();
        Long filesRepositoryItemId = getFilesRepositoryItemId(request);
        String fileDescription = getFileRepositoryItemDescription(request);
        try {
            filesRepoFacade.updateDescription(filesRepositoryItemId, fileDescription);
        } catch (UpdateDescriptionException e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot modify description of file in repository with item id=[" + filesRepositoryItemId + "].", e);
            result.addError("Cannot modify description of file in repository with item id=[" + filesRepositoryItemId + "].", e.getMessage());
            return result;
        }
        return result;
    }

    @ControllerMethod(action = "updateSendWithMail")
    public GenericResultBean updateSendWithMail(final OsgiWebRequest invocation) {
        GenericResultBean result = new GenericResultBean();
        HttpServletRequest request = invocation.getRequest();
        Long filesRepositoryItemId = getFilesRepositoryItemId(request);
        String sendWithMailString = request.getParameter(FILE_SENDWITHMAIL_PARAM_NAME);

        try {
            Boolean sendWithMail = Boolean.parseBoolean(sendWithMailString);
            filesRepoFacade.updateSendWithMail(filesRepositoryItemId, sendWithMail);
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "[FILES_REPOSITORY] Cannot modify sendWithHtml of file in repository with item id=[" + filesRepositoryItemId + "].", e);
            result.addError("Cannot modify sendWithHtml of file in repository with item id=[" + filesRepositoryItemId + "].", e.getMessage());
            return result;
        }
        return result;
    }

    private void sendInResponseOutputStream(HttpServletRequest request, HttpServletResponse response,
                                            FileItemContent content) throws IOException {

        try {

            String encoding = request.getCharacterEncoding();

            String disposition = "attachment; filename=\"" + URLEncoder.encode(content.getName(), "UTF-8") + "\"; "
                    + "filename*=UTF-8''" + URLEncoder.encode(content.getName(), "UTF-8");

            response.setHeader("Content-disposition", disposition);
            response.setContentType(content.getContentType());
            response.setCharacterEncoding("UTF-8");
            ServletOutputStream soutStream = response.getOutputStream();
            IOUtils.write(content.getBytes(), soutStream);
            IOUtils.closeQuietly(soutStream);

        } catch(UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Download file error", e);
        }
    }

    private Long getFilesRepositoryItemId(HttpServletRequest request) {
        String filesRepositoryItemIdStr = request.getParameter(FILES_REPOSITORY_ITEM_ID_REQ_PARAM_NAME);
        Long filesRepositoryItemId = filesRepositoryItemIdStr != null ? Long.valueOf(filesRepositoryItemIdStr) : null;
        if (filesRepositoryItemId == null) {
            throw new RuntimeException("FilesRepositoryItem ID not provided in request!");
        }
        return filesRepositoryItemId;
    }

    private String getCreatorLogin(HttpServletRequest request) {
        UserData user = portalUserSource.getUserByRequest(request);
        return user.getLogin();
    }

    private Long getProcessInstanceId(HttpServletRequest request) {
        String processInstanceIdStr = request.getParameter(PROCESS_INSTANCE_ID_REQ_PARAM_NAME);
        processInstanceIdStr = processInstanceIdStr.replaceAll("\u00a0+", "");
        Long processInstanceId = processInstanceIdStr != null ? Long.valueOf(processInstanceIdStr) : null;
        if (processInstanceId == null) {
            throw new RuntimeException("Process instance ID not provided in request!");
        }
        return processInstanceId;
    }

    private String getFileRepositoryItemDescription(HttpServletRequest request) {
        String fileDescription = request.getParameter(FILE_DESCRIPTION_PARAM_NAME);
        return fileDescription;
    }
}
