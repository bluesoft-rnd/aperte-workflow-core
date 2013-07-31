package org.aperteworkflow.sample.process.incident.controller;

import java.util.List;

import org.aperteworkflow.sample.process.incident.model.Employee;
import org.aperteworkflow.sample.process.incident.services.EmployeeService;

import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiController;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;

@OsgiController(name="incidentcontroler")
public class MainController implements IOsgiWebController{
 
    @ControllerMethod(action="getAvailableEmployees")
	public GenericResultBean getAvailableEmployees(final OsgiWebRequest invocation) {
    	GenericResultBean result = new GenericResultBean();

    	String issueType = invocation.getRequest().getParameter("issueType");
    	String incidentType = invocation.getRequest().getParameter("incidentType");
    	
    	EmployeeService employeeService = ObjectFactory.create(EmployeeService.class);
    	List<Employee> employees = employeeService.getAvailableEmployees(issueType, incidentType);
    	
    	result.setData(employees);
    	
    	return result;
	}
	
}
