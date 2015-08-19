package org.aperteworkflow.samples.application.controller;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.aperteworkflow.samples.application.model.ApplicationForm;
import org.aperteworkflow.samples.application.service.RegisterApplicationRequestType;
import org.aperteworkflow.samples.application.service.RegisterApplicationResponseType;
import org.aperteworkflow.samples.application.ws.WsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Dominik Dębowczyk on 2015-07-31.
 */
@Controller
public class ApplicationFormController {

    private static final Logger log = Logger.getLogger(ApplicationFormController.class);

    @Autowired
    WsClient wsClient;
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView main() {

        return new ModelAndView("index.jsp", "applicationForm", new ApplicationForm());
    }

    @RequestMapping(value = "/apply", method = RequestMethod.POST)
    public ModelAndView apply(@ModelAttribute("SpringWeb") ApplicationForm applicationForm,
                              ModelMap model) {

        CommonsMultipartFile file = applicationForm.getAttachmentFile();
        RegisterApplicationRequestType req = new RegisterApplicationRequestType();
        req.setName(applicationForm.getName());
        req.setSurname(applicationForm.getSurname());
        req.setDescription(applicationForm.getDescription());
        if(file!=null) {
            req.setAttachmentBase64(getFileString(file));
            req.setAttachmentName(file.getOriginalFilename());
            req.setAttachmentMimeType(file.getContentType());
        }
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index.jsp");
        modelAndView.addObject("applicationForm", new ApplicationForm());
        try {
           RegisterApplicationResponseType res = wsClient.getService().registerApplication(req);
            if("OK".equals(res.getStatus()))
                modelAndView.addObject("applicationSent", true);
            else {
                modelAndView.addObject("applicationSent", false);
            }
        } catch (Exception e) {
            log.error(e);
            modelAndView.addObject("applicationSent", false);
        }
        return modelAndView;
    }

    private String getFileString(CommonsMultipartFile file) {
        String fileBase64 = null;
            try {
                byte[] bytes = Base64.encodeBase64(file.getBytes());
                fileBase64 = new String(bytes);
            } catch (Exception e) {
                log.error(e);
            }
        return fileBase64;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        log.warn("Returning HTTP 400 Bad Request", e);
    }

}
