package org.aperteworkflow.portlets;

import com.vaadin.terminal.gwt.server.ApplicationPortlet2;
import org.apache.commons.lang.StringEscapeUtils;
import org.aperteworkflow.bpm.graph.GraphElement;
import org.aperteworkflow.bpm.graph.StateNode;
import org.aperteworkflow.bpm.graph.TransitionArc;
import org.aperteworkflow.bpm.graph.TransitionArcPoint;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.util.i18n.I18NSourceFactory;
import pl.net.bluesoft.rnd.util.i18n.impl.DefaultI18NSource;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import javax.imageio.ImageIO;
import javax.portlet.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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

            ProcessToolRegistry registry = (ProcessToolRegistry) getPortletConfig()
                    .getPortletContext().getAttribute(ProcessToolRegistry.class.getName());
            registry.withProcessToolContext(new ProcessToolContextCallback() {
                @Override
                public void withContext(ProcessToolContext ctx) {
                    ProcessToolContext.Util.setThreadProcessToolContext(ctx);
                    try {
                        try {
                            I18NSource.ThreadUtil.setThreadI18nSource(I18NSourceFactory.createI18NSource(request.getLocale()));
                            if (request instanceof ResourceRequest) {
                                ResourceRequest rr = (ResourceRequest) request;
                                ResourceResponse resp = (ResourceResponse) response;
                                if (rr.getParameter("instanceId") != null) { //special handling
                                    logger.info("Image request");
                                    ProcessToolBpmSession session = ctx.getProcessToolSessionFactory()
                                            .createSession(new UserData("admin", "admin@aperteworkflow.org", "Admin admin"),
                                                    new ArrayList<String>());
                                    byte[] bytes = session.getProcessMapImage(
                                            session.getProcessData(rr.getParameter("instanceId"), ctx));
                                    if (bytes != null) {
                                        resp.setContentType("image/png");
                                        resp.getPortletOutputStream().write(bytes);
                                    }
                                    return;
                                } else if (rr.getParameter("svg") != null) { //to use svg inside of a window
                                    logger.info("SVG request");

                                    ProcessToolBpmSession session = ctx.getProcessToolSessionFactory()
                                            .createSession(new UserData("admin", "admin@aperteworkflow.org", "Admin admin"), 
                                                    new ArrayList<String>());
                                    ProcessInstance pi = session.getProcessData(rr.getParameter("svg"), ctx);
                                    List<GraphElement> processHistory = session.getProcessHistory(pi);
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

                                        for (GraphElement el : processHistory) {
                                            if (el instanceof StateNode) {
                                                StateNode sn = (StateNode) el;
                                                String fill = sn.isUnfinished() ? "fill:#1B59E0;fill-opacity:0.3" : "fill-opacity:0.0";
                                                svg.append(String.format("<rect x=\"%d\" y=\"%d\" height=\"%d\" width=\"%d\"\n" +
                                                                    " rx=\"5\" ry=\"5\"\n" +
                                                                    " style=\"" + strokeStyle + fill + "\"/>\n",
                                                                    sn.getX(),
                                                                    sn.getY(),
                                                                    sn.getHeight(),
                                                                    sn.getWidth()));
                                            } else if (el instanceof TransitionArc) {
                                                TransitionArc ta = (TransitionArc) el;
                                                TransitionArcPoint prevPoint = null;
                                                for (TransitionArcPoint p : ta.getPath()) {
                                                    if (prevPoint != null) {
                                                        svg.append(String.format("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\"\n" +
                                                                "  style=\"" + strokeStyle + "\"/>\n",
                                                                prevPoint.getX(),
                                                                prevPoint.getY(),
                                                                p.getX(),
                                                                p.getY()
                                                                ));
                                                    }
                                                    prevPoint = p;
                                                }
                                            }
                                        }
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
                    } finally {
                        ProcessToolContext.Util.removeThreadProcessToolContext();
                    }
                }
            });
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }


    }
}
