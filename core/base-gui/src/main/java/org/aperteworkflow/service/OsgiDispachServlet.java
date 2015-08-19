package org.aperteworkflow.service;

import org.aperteworkflow.osgi.OsgiServiceDispatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 15.05.14
 * Time: 12:59
 */
@Deprecated
public class OsgiDispachServlet extends HttpServlet {


	private WebApplicationContext applicationContext;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp);
	}

	protected void  process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml");
		PrintWriter writer = resp.getWriter();
		String response = getOsgiDispatcher().handle(req);
		writer.print(response);
		writer.close();
	}


	private OsgiServiceDispatcher getOsgiDispatcher() {
		return getContext().getBean(OsgiServiceDispatcher.class);
	}

	private WebApplicationContext getContext() {
		if (applicationContext == null){
			System.out.println("setting context in get");
			applicationContext = WebApplicationContextUtils.getWebApplicationContext(this.getServletContext());
			if(applicationContext == null){
				throw  new IllegalStateException("Application context not found!");
			}
		}
		return applicationContext;
	}
}
