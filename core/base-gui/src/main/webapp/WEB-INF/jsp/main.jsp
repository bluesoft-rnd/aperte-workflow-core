<!-- Main View for Aperte Workflow Activites -->
<!-- @author: mpawlak@bluesoft.net.pl -->

<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!--<script language='javascript' src='/aperteworkflow/VAADIN/widgetsets/pl.net.bluesoft.rnd.widgetset.PortalDefaultWidgetSet/pl.net.bluesoft.rnd.widgetset.PortalDefaultWidgetSet.nocache.js'></script>-->
<!--<script src="<%=request.getContextPath()%>/js/mp-admin-utils.js" ></script>-->

<h2>Aperte Workflow Maginificient Activities</h2>
<c:if test="${aperteUser.login!=null}">
UserLogin: ${aperteUser.login} <br>

<div class="main-view">
	<%@include file="leftMenu.jsp" %>
	<%@include file="processList.jsp" %>
	<%@include file="widgetList.jsp" %>
	<%@include file="actionsList.jsp" %>
	<%@include file="processStartList.jsp" %>
</div>

</c:if> 
 <c:if test="${aperteUser.login==null}">
	<div class="please-log-in-label">
		<spring:message code="authorization.please.log.in" />
	</div>
 </c:if> 
 
  <script type="text/javascript">
	$(window).unload(function() 
	{
		clearProcessView();
	});
 
	function showNewProcessPanel()
	{
		clearProcessView();
		
		$('#process-data-view').hide();
		$('#actions-list').hide();
		$('#process-panel-view').hide();
		$('#new-process-view').show();
    }
	
	function showProcessList()
	{
		clearProcessView();
	
		
		$('#process-data-view').hide();
		$('#actions-list').hide();
		$('#process-panel-view').show();
		$('#new-process-view').hide();

    }
	
	function showProcessData()
	{
		$('#process-data-view').show();
		$('#actions-list').show();
		$('#process-panel-view').hide();
		$('#new-process-view').hide();
    }
	
	function clearProcessView()
	{
		<!-- required to close vaadin application -->
		$('.vaadin-widget-view').each(function( ) 
		{ 
			var widgetId = $(this).attr('widgetId');
			var taskId = $(this).attr('taskId');
			
			var windowName = taskId+"_"+widgetId;
			console.log( "close! windowName: "+windowName);
			
			var source = "widget/"+windowName+"_close/";
			var url = '<spring:url value="/'+source+'"/>';
			
			console.log( "close! url: "+url);
			
			$.getJSON(url)
			.done(function() {
			  console.log( "killed!");
			});
			
		});
		
		vaadinWidgetsCount = 0;
		vaadinWidgetsLoadedCount = 0;
	}
 
 </script>