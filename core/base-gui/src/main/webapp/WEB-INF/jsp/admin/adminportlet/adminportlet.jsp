<!-- Aperte Workflow Admin Panel -->
<!-- @author: polszewski@bluesoft.net.pl -->

<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@include file="../../utils/globals.jsp" %>
<%@include file="../../utils/apertedatatable.jsp" %>

<%@ page import="java.util.*" %>
<%@ page import="pl.net.bluesoft.rnd.processtool.plugins.*" %>
<%@ page import="pl.net.bluesoft.rnd.processtool.portlets.generic.*" %>
<%@ page import="org.aperteworkflow.ui.view.*" %>
<%@ page import="pl.net.bluesoft.util.lang.cquery.*" %>
<%@ page import="pl.net.bluesoft.util.lang.cquery.func.*" %>

<%
List<GenericPortletViewRenderer> renderers = getPermittedRenderers(PortletKeys.ADMIN);

for (GenericPortletViewRenderer renderer : renderers) {
%>

<div class="process-queue-name apw_highlight"><spring:message code="<%= renderer.getName() %>"/></div>

<%= renderer.render() %>

<%
}
%>

<%!

private List<GenericPortletViewRenderer> getPermittedRenderers(String portletKey) {
	ProcessToolRegistry registry = ProcessToolRegistry.Util.getRegistry();
	IViewRegistry viewRegistry = registry.getRegisteredService(IViewRegistry.class);
	List<GenericPortletViewRenderer> permittedViews = new ArrayList<GenericPortletViewRenderer>();

	for (GenericPortletViewRenderer renderer : viewRegistry.getGenericPortletViews(portletKey)) {
		if (isPermitted(renderer)) {
			permittedViews.add(renderer);
		}
	}

	permittedViews = arrangeViews(permittedViews);

	return permittedViews;
}

private List<GenericPortletViewRenderer> arrangeViews(List<GenericPortletViewRenderer> permittedViews) {
	return CQuery.from(permittedViews).orderBy(new F<GenericPortletViewRenderer, Comparable>() {
		@Override
		public Comparable invoke(GenericPortletViewRenderer x) {
			return x.getPosition();
		}
	}).toList();
}

private boolean isPermitted(GenericPortletViewRenderer renderer) {
	return hasRoles(renderer.getRequiredRoles());
}

private boolean hasRoles(String[] requiredRoles) {
	if (requiredRoles != null) {
		for (String role : requiredRoles) {
			if (false) { // TODO
				return false;
			}
		}
	}
	return true;
}
%>