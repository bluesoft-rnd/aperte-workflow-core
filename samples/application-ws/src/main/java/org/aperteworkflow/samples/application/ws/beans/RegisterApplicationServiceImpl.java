package org.aperteworkflow.samples.application.ws.beans;


import org.apache.commons.lang.StringUtils;
import org.aperteworkflow.files.IFilesRepositoryFacade;
import org.aperteworkflow.files.dao.FilesRepositoryAttributeFactory;
import org.aperteworkflow.files.dao.FilesRepositoryProcessAttributeFactoryImpl;
import org.aperteworkflow.files.exceptions.UploadFileException;
import org.aperteworkflow.samples.application.service.RegisterApplicationRequestType;
import org.aperteworkflow.samples.application.service.RegisterApplicationResponseType;
import org.aperteworkflow.samples.application.service.RegisterApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolSessionFactory;
import pl.net.bluesoft.rnd.processtool.bpm.StartProcessResult;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import org.apache.commons.codec.binary.Base64;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.jws.WebParam;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;

/**
 * Created by Dominik Dębowczyk on 2015-08-03.
 */
public class RegisterApplicationServiceImpl implements RegisterApplicationService {

    private final Logger logger = Logger.getLogger(RegisterApplicationServiceImpl.class.getName());
    @Autowired
    private ProcessToolRegistry registry;

    @Autowired
    private ProcessToolSessionFactory jbpmSessionFactory;

    @Autowired
    protected IFilesRepositoryFacade filesRepoFacade;

    @Override
    public RegisterApplicationResponseType registerApplication(@WebParam(partName = "name", name = "name", targetNamespace = "") final RegisterApplicationRequestType req) {
        final RegisterApplicationResponseType res = new RegisterApplicationResponseType();
        try{

            registry.withExistingOrNewContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext context) {
                    registerApplication(req, res, context);
                }
            });
            return  res;
        } catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage(), e);
            res.setStatus("ERROR");
        }
        return res;
    }

    private void registerApplication(RegisterApplicationRequestType req, RegisterApplicationResponseType res, ProcessToolContext context) {
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("name", req.getName());
        attributes.put("surname", req.getSurname());
        attributes.put("description", req.getDescription());
        attributes.put("isWs", "true");

        ProcessToolBpmSession jbpmSession = jbpmSessionFactory.createAutoSession();
        StartProcessResult startProcessResult = jbpmSession.startProcess("application.process.name", null, RegisterApplicationServiceImpl.class.getName(), attributes);
        ProcessInstance processInstance = startProcessResult.getProcessInstance();
        res.setProcessId(processInstance.getId().toString());
        res.setStatus("OK");
        try {
            saveFileInRepository(req, processInstance);
        } catch (UploadFileException e) {
            e.printStackTrace();
        }
    }

    private void saveFileInRepository(RegisterApplicationRequestType req, ProcessInstance instance) throws UploadFileException {
        String attachment = req.getAttachmentBase64();
        if(StringUtils.isNotEmpty(attachment)) {
            byte [] fileBytes = Base64.decodeBase64(attachment);
            ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes);
            filesRepoFacade.uploadFile(bis, req.getAttachmentMimeType(), instance, req.getAttachmentName(), "attachment from ws", "ws-test", getAttributesFactory());
        }
    }

    private FilesRepositoryAttributeFactory getAttributesFactory() {
        return FilesRepositoryProcessAttributeFactoryImpl.INSTANCE;
    }
}
