package pl.net.bluesoft.rnd.processtool.web.controller;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
public class OsgiWebRequest
{
    private HttpServletRequest request;
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
}
