package org.aperteworkflow.portlets;

import com.vaadin.Application;
import com.vaadin.ui.Window;
import org.aperteworkflow.ui.processadmin.ProcessInstanceAdminManagerPane;
import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;

/**
 *
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceManagerPortlet extends GenericVaadinPortlet2BpmApplication {
    public static final String AWF__INSTANCE_ID = "__AWF__INSTANCE_ID";
    private String processInstanceMapRequestUrl = null;
    
    @Override
    protected void initializePortlet() {
    }

    @Override
    protected void renderPortlet() {
        getMainWindow().setContent(new ProcessInstanceAdminManagerPane(this, bpmSession));
    }

    @Override
    public void handleResourceRequest(ResourceRequest request, ResourceResponse response, Window window) {        
        super.handleResourceRequest(request, response, window);
        ResourceURL resourceURL = response.createResourceURL();
        resourceURL.setParameter("svg", AWF__INSTANCE_ID);
        processInstanceMapRequestUrl = resourceURL.toString();
    }
    
    public static String getProcessInstanceMapRequestUrl(Application app, String instanceId) {
        if (app instanceof ProcessInstanceManagerPortlet) {
            return ((ProcessInstanceManagerPortlet)app).processInstanceMapRequestUrl.replace(AWF__INSTANCE_ID, instanceId);
        } else {
            throw new IllegalArgumentException("not supported: " + app.getClass());
        }
    }
}
