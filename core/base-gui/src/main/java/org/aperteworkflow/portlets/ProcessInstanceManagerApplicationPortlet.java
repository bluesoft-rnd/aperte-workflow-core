package org.aperteworkflow.portlets;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import org.apache.commons.lang.StringEscapeUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;

import javax.imageio.ImageIO;
import javax.portlet.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessInstanceManagerApplicationPortlet extends ApplicationPortlet2 {

    private static final Logger logger = Logger.getLogger(ProcessInstanceManagerApplicationPortlet.class.getName());

    @Override
    protected void handleRequest(final PortletRequest request, final PortletResponse response) throws PortletException, IOException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            getRegistry().withProcessToolContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext ctx) {
                    try {
                        try {
                            I18NSource.ThreadUtil.setThreadI18nSource(I18NSourceFactory.createI18NSource(request.getLocale()));
                            if (request instanceof ResourceRequest) {
                                ResourceRequest rr = (ResourceRequest) request;
                                ResourceResponse resp = (ResourceResponse) response;
                                if (rr.getParameter("instanceId") != null) { //special handling
                                    logger.info("Image request");
                                    ProcessToolBpmSession session = getRegistry().getProcessToolSessionFactory()
                                            .createSession("admin");
                                    byte[] bytes = session.getProcessMapImage(
											ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(rr.getParameter("instanceId")));
                                    if (bytes != null) {
                                        resp.setContentType("image/png");
                                        resp.getPortletOutputStream().write(bytes);
                                    }
                                    return;
                                } else if (rr.getParameter("svg") != null) { //to use svg inside of a window
                                    logger.info("SVG request");

                                    ProcessToolBpmSession session = getRegistry().getProcessToolSessionFactory()
                                            .createSession("admin");
                                    ProcessInstance pi = ctx.getProcessInstanceDAO().getProcessInstanceByInternalId(rr.getParameter("svg"));

//                                    final StringBuffer svg = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
                                    final StringBuffer svg = new StringBuffer("<html><body style=\"margin:0; padding:0\">\n\n");
                                    
                                    final byte[] png = session.getProcessMapImage(pi);
                                    if (png != null) {
                                        BufferedImage read;
                                        try {
                                            read = ImageIO.read(new ByteArrayInputStream(png));
                                            ResourceURL resourceURL = resp.createResourceURL();
                                            resourceURL.setParameter("instanceId", pi.getInternalId());
                                            String url = resourceURL.toString();
                                            url = StringEscapeUtils.escapeXml(url);
                                            svg.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\"\n" + "   viewBox='0 0 %d %d'  " +
                                                    "     xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n",
                                                    read.getWidth(),
                                                    read.getHeight()));
                                            svg.append(String.format("<image x=\"0\" y=\"0\" width=\"%d\" height=\"%d\"\n" +
                                                    "xlink:href=\"%s\" />",
                                                    read.getWidth(),
                                                    read.getHeight(),
                                                    url));
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }                                        

                                        String strokeStyle = "stroke:#1B59E0;stroke-width:5;opacity: 1;";


                                        svg.append("</svg></body></html>");
                                        resp.setContentType("text/html");
                                        resp.getPortletOutputStream().write(svg.toString().getBytes());
                                    }
                                }
                            }
                            ProcessInstanceManagerApplicationPortlet.super.handleRequest(request, response);
                        } finally {
                            I18NSource.ThreadUtil.removeThreadI18nSource();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } 
                }
            });
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
}
