package pl.net.bluesoft.rnd.processtool.web.controller;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 * @author: "pwysocki@bluesoft.net.pl"
 */
public class OsgiWebRequest {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ProcessToolContext processToolContext;
    private IProcessToolRequestContext processToolRequestContext;

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public IProcessToolRequestContext getProcessToolRequestContext() {
        return processToolRequestContext;
    }

    public void setProcessToolRequestContext(IProcessToolRequestContext processToolRequestContext) {
        this.processToolRequestContext = processToolRequestContext;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public ProcessToolContext getProcessToolContext() {
        return processToolContext;
    }

    public void setProcessToolContext(ProcessToolContext processToolContext) {
        this.processToolContext = processToolContext;
    }
}
