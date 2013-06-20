<!-- Main View for Aperte Workflow Activites -->
<!-- @author: mpawlak@bluesoft.net.pl -->

<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!--<script language='javascript' src='/aperteworkflow/VAADIN/widgetsets/pl.net.bluesoft.rnd.widgetset.PortalDefaultWidgetSet/pl.net.bluesoft.rnd.widgetset.PortalDefaultWidgetSet.nocache.js'></script>-->
<!--<script src="<%=request.getContextPath()%>/js/mp-admin-utils.js" ></script>-->

<!--<h2>Aperte Workflow Maginificient Activities</h2>-->
<c:if test="${aperteUser.login!=null}">

<div class="main-view">
	<%@include file="leftMenu.jsp" %>
	<%@include file="processList.jsp" %>
	<%@include file="widgetList.jsp" %>
	<%@include file="actionsList.jsp" %>
	<%@include file="processStartList.jsp" %>
	<%@include file="searchView.jsp" %>
	<%@include file="configuration.jsp" %>
</div>

</c:if>  
 <c:if test="${aperteUser.login==null}">
	<%@include file="login.jsp" %>
 </c:if> 
 
  <script type="text/javascript">
  
  	$(window).unload(function() 
	{
		windowManager.clearProcessView();
		
	});
	
	function XOR(a,b) {
	  return ( a || b ) && !( a && b );
	} 
	
	var windowManager = new WindowManager();
  
	function WindowManager()
	{
		this.currentView = 'process-panel-view';
		this.viewHistory = [];
		this.allViews = ["process-data-view", "actions-list", "process-panel-view", "new-process-view", "search-view", "outer-queues", "configuration"];
		
		this.previousView = function()
		{
			var lastView = this.viewHistory.pop();
			console.log( "lastView "+lastView);
			if(lastView)
			{
				this.showView(lastView, false);
			}
		}
		
		this.showQueueList = function()
		{
			this.showView('outer-queues', true);
		}
		
		this.showConfiguration = function()
		{
			this.showView('configuration', true);
		}
		
		this.hasPreviousView = function()
		{
			return this.viewHistory.length > 0;
		}
		
		
		
		this.showSearchProcessPanel = function()
		{
			this.showView('search-view', true);
		};
		
		this.showView = function(viewName, addToHistory)
		{
			windowManager.clearProcessView();
			
			$.each(this.allViews, function( ) 
			{ 
				var elementId = this;
				if(this != viewName)
				{
					$(document.getElementById(elementId)).hide();
				}
			});
			
			if(addToHistory == true)
			{
				this.viewHistory.push(this.currentView);
			}
			
			this.currentView = viewName;
			$(document.getElementById(viewName)).fadeIn(500);
		}
		
		this.showNewProcessPanel = function()
		{
			this.showView('new-process-view', true);
		};
		
		this.showProcessList = function()
		{
			this.showView('process-panel-view', true);
		}
		
		this.showProcessData = function()
		{
			this.showView('process-data-view', true);
			$('#actions-list').fadeIn(600);
		}
		
		this.clearProcessView = function()
		{
			$('#actions-list').empty();
			
			widgets = [];
			
			<!-- required to close vaadin application -->
			$('.vaadin-widget-view').each(function( ) 
			{ 
				var widgetToClose = $(this);
				var widgetId = $(this).attr('widgetId');
				var taskId = $(this).attr('taskId');
				
				var windowName = taskId+"_"+widgetId;
				
				var source = "widget/"+windowName+"_close/";
				var url = '<spring:url value="/'+source+'"/>';
				
				console.log( "close! url: "+url);
				
				$.ajax(url)
				.done(function() 
				{
					widgetToClose.remove();
				  console.log( "killed!");
				});
				
			});
			
			vaadinWidgetsCount = 0;
			vaadinWidgetsLoadedCount = 0;
		}
	}
  

	

 
	
 
 </script>