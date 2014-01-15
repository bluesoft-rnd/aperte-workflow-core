package org.aperteworkflow.files.controller;

import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

import java.util.logging.Logger;


@OsgiController(name="filescontroller")
public class FilesController implements IOsgiWebController
{
    private static Logger logger = Logger.getLogger(FilesController.class.getName());

//    @Autowired
//    private IMHRFacade mhrFacade;


    @ControllerMethod(action="uploadFile")
    public GenericResultBean getAvailableEmployees(final OsgiWebRequest invocation)
    {

        GenericResultBean result = new GenericResultBean();

        //result.setData(mhrFacade.getCompanies());

        return result;
    }


}
