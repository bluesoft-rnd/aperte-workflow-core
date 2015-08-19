package pl.net.bluesoft.rnd.processtool.web.controller;

import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by mpawlak@bluesoft.net.pl on 2015-03-23.
 */
public class NoTransactionOsgiWebRequest {
    private HttpServletRequest request;
    private HttpServletResponse response;

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

}
