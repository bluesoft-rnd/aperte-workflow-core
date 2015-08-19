package pl.net.bluesoft.casemanagement.processor;

import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.model.FilesRepositoryItem;
import org.aperteworkflow.files.model.IFilesRepositoryAttribute;
import org.aperteworkflow.files.model.IFilesRepositoryItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.casemanagement.dao.FilesRepositoryCaseStageAttributeFactoryImpl;
import pl.net.bluesoft.casemanagement.model.*;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.plugins.IMapper;
import pl.net.bluesoft.rnd.processtool.plugins.Mapper;
import pl.net.bluesoft.rnd.processtool.plugins.MapperContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static pl.net.bluesoft.casemanagement.model.util.CaseModelUtil.getCaseComments;
import static pl.net.bluesoft.casemanagement.model.util.CaseModelUtil.getFiles;


/**
 * Created by pkuciapski on 2014-05-08.
 */
@Mapper(forProviderClass = ProcessInstance.class, forDefinitionNames = {})
public class ProcessInstanceMapper implements IMapper<ProcessInstance> {
    private final Logger logger = Logger.getLogger(ProcessInstanceMapper.class.getName());

    @Autowired
    private IFilesRepositoryFacade filesRepositoryFacade;

    @Override
    public void map(IAttributesConsumer consumer, ProcessInstance provider, MapperContext mapperContext) {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        copyProcessComments(provider, consumer, mapperContext);
        copyFilesRepositoryItems(provider, (Case) consumer, mapperContext);
    }

    private void copyFilesRepositoryItems(final ProcessInstance provider, final Case caseInstance, MapperContext mapperContext) {
        List<IFilesRepositoryItem> attachments = new ArrayList<IFilesRepositoryItem>();

        // copy all files repository items
        for (IFilesRepositoryItem fri : filesRepositoryFacade.getFilesList(provider)) {
            /** Email attachments are added when the email is sent, skip it */
            if (Boolean.TRUE.equals(fri.getSendWithMail()))
                continue;

            final FilesRepositoryItem caseItem = new FilesRepositoryItem();
            caseItem.setCreateDate(fri.getCreateDate());
            caseItem.setName(fri.getName());
            caseItem.setContentType(fri.getContentType());
            caseItem.setCreatorLogin(fri.getCreatorLogin());
            caseItem.setDescription(fri.getDescription());
            caseItem.setRelativePath(fri.getRelativePath());
            IFilesRepositoryAttribute caseAttr = getFiles(caseInstance);
            caseAttr.getFilesRepositoryItems().add(caseItem);
            attachments.add(caseItem);
            copyFileToCurrentStage(mapperContext, caseItem);
        }
        CaseMapperContextParams.setAttachments(mapperContext, attachments);

    }

    private void copyFileToCurrentStage(MapperContext mapperContext, FilesRepositoryItem attachment) {
        CaseStage currentStage = CaseMapperContextParams.getStage(mapperContext);
        FilesRepositoryCaseStageAttribute stageAttribute = (FilesRepositoryCaseStageAttribute) currentStage.getAttribute(CaseStageAttributes.STAGE_FILES.value());
        if (stageAttribute == null) {
            stageAttribute = (FilesRepositoryCaseStageAttribute) FilesRepositoryCaseStageAttributeFactoryImpl.INSTANCE.create();
            currentStage.setAttribute(CaseStageAttributes.STAGE_FILES.value(), stageAttribute);
        }
        stageAttribute.getFilesRepositoryItems().add(attachment);
    }

    /**
     * Copy all process comments
     */
    private void copyProcessComments(final ProcessInstance pi, final IAttributesConsumer consumer, MapperContext mapperContext) {
        // copy all process comments
        for (ProcessComment comment : pi.getComments()) {
            if (comment.getBody() == null)
                continue;
            final String key = CaseAttributes.COMMENTS.value();
            CaseCommentsAttribute attribute = getCaseComments(consumer);
            if (attribute == null) {
                attribute = new CaseCommentsAttribute();
                consumer.setAttribute(key, attribute);
            }
            CaseComment caseComment = new CaseComment();
            caseComment.setAuthorFullName(comment.getAuthorFullName());
            caseComment.setBody(comment.getBody());
            caseComment.setAuthorLogin(comment.getAuthorLogin());
            caseComment.setCommentType(comment.getCommentType());
            caseComment.setCreateDate(comment.getCreateTime());
            caseComment.setProcessState(comment.getProcessState());
            attribute.getComments().add(caseComment);
            copyCaseCommentToCurrentStage(mapperContext, consumer, caseComment);
        }
    }

    private void copyCaseCommentToCurrentStage(MapperContext mapperContext, final IAttributesConsumer consumer, CaseComment comment) {
        CaseStage currentStage = CaseMapperContextParams.getStage(mapperContext);
        final String key = CaseStageAttributes.COMMENTS.value();
        CaseStageCommentsAttribute attribute = (CaseStageCommentsAttribute) currentStage.getAttribute(CaseStageAttributes.COMMENTS.value());
        if (attribute == null) {
            attribute = new CaseStageCommentsAttribute();
            currentStage.setAttribute(key, attribute);
        }
        attribute.getComments().add(comment);
    }
}
