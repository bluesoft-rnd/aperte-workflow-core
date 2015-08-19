package org.aperteworkflow.samples.application.service;

import org.aperteworkflow.samples.application.service.ObjectFactory;
import org.aperteworkflow.samples.application.service.RegisterApplicationRequestType;
import org.aperteworkflow.samples.application.service.RegisterApplicationResponseType;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.4.4
 * 2015-08-03T11:57:56.232+02:00
 * Generated source version: 2.4.4
 * 
 */
@WebService(targetNamespace = "http://www.aperteworkflow.org/samples/", name = "RegisterApplicationService")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface RegisterApplicationService {

    @WebResult(name = "NewPart", targetNamespace = "", partName = "NewPart")
    @WebMethod(operationName = "RegisterApplication", action = "http://www.aperteworkflow.org/incident/RegisterApplication")
    public RegisterApplicationResponseType registerApplication(
        @WebParam(partName = "name", name = "name", targetNamespace = "")
        RegisterApplicationRequestType name
    );
}
