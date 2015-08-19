<!-- Main View for Aperte Workflow Activites -->
<!-- @author: mpawlak@bluesoft.net.pl -->

<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!--<script language='javascript' src='/aperteworkflow/VAADIN/widgetsets/pl.net.bluesoft.rnd.widgetset.PortalDefaultWidgetSet/pl.net.bluesoft.rnd.widgetset.PortalDefaultWidgetSet.nocache.js'></script>-->
<!--<script src="<%=request.getContextPath()%>/js/mp-admin-utils.js" ></script>-->

<!--<h2>Aperte Workflow Maginificient Activities</h2>-->
<%@include file="utils/windowManager.jsp" %>
<%@include file="utils/queuemanager.jsp" %>
<%@include file="utils/globals.jsp" %>

<c:if test="${aperteUser.login!=null}">

<div class="apw main-view">
	
	<%@include file="leftMenu.jsp" %>
	<%@include file="taskView.jsp" %>
	<%@include file="widgetList.jsp" %>
	<%@include file="actionsList.jsp" %>
	<%@include file="processStartList.jsp" %>
	<%@include file="configuration.jsp" %>
	<div id="error-screen" class="errors-view" hidden="true"></div>
	<div id="loading-screen" class="loader-2"></div>
	<div hidden id="saving-screen" class="loader-2 saver"></div>
</div>

</c:if>  
 <c:if test="${aperteUser.login==null}">
	<%@include file="login.jsp" %>
 </c:if> 
 
  <script type="text/javascript">

  
  	$(document).ready(function()
	{
		// bootstrap moodals fix for ie7
		$(".modal").appendTo($("body"));
		
		windowManager.addView("error-screen");
		windowManager.addView("loading-screen");
		<c:if test="${aperteUser.login!=null}">
			<c:choose>
				  <c:when test="${externalTaskId!=null}">
						 loadProcessView("${externalTaskId}");
				  </c:when>
				  <c:otherwise>
						windowManager.showProcessList();
				  </c:otherwise>
			</c:choose>
			reloadQueues();
			moveQueueList();
		</c:if> 

	});
  
  	$(window).unload(function() 
	{
		windowManager.clearProcessView();
		
	});
	

  
  

	

 
	
 
 </script>