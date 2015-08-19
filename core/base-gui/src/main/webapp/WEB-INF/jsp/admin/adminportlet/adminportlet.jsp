<!-- Aperte Workflow Admin Panel -->
<!-- @author: polszewski@bluesoft.net.pl -->

<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@include file="../../utils/globals.jsp" %>


<c:forEach items="${renderers}" var="renderer">
	<div class="process-queue-name apw_highlight"><spring:message code="${renderer.name}"/></div>
		${renderer.code}
</c:forEach>
