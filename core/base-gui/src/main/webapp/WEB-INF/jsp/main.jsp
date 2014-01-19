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

<div class="apw main-view">
	<%@include file="leftMenu.jsp" %>
	<%@include file="taskView.jsp" %>
	<%@include file="widgetList.jsp" %>
	<%@include file="actionsList.jsp" %>
	<%@include file="processStartList.jsp" %>
	<%@include file="searchView.jsp" %>
	<%@include file="configuration.jsp" %>
	<div id="error-screen" class="errors-view" hidden="true"></div>
	<div id="loading-screen" class="loader-2"></div>
</div> 

</c:if>  
 <c:if test="${aperteUser.login==null}">
	<%@include file="login.jsp" %>
 </c:if> 
 
  <script type="text/javascript">
    var dispatcherPortlet = '<portlet:resourceURL id="dispatcher"/>';
	var windowManager = new WindowManager();
  
  	$(document).ready(function()
	{
		windowManager.addView("error-screen");
		windowManager.addView("loading-screen");
		<c:choose>
              <c:when test="${externalTaskId!=null}">
                     loadProcessView("${externalTaskId}");
              </c:when>

              <c:otherwise>
                      windowManager.showProcessList();
              </c:otherwise>
        </c:choose>

		externalTaskId
		

		reloadQueues();
		moveQueueList();
	});
  
  	$(window).unload(function() 
	{
		windowManager.clearProcessView();
		
	});
	
	function XOR(a,b) {
	  return ( a || b ) && !( a && b );
	} 
	
	function WindowView(viewId)
	{
		this.viewId = viewId;
	}

  
	function WindowManager()
	{
		this.currentView = 'process-panel-view';
		this.viewHistory = [];
		this.mobileMode = false;
		this.tabletMode = false;
		this.allViews = {};
		
		this.addView = function(viewId)
		{
			this.allViews[viewId] = new WindowView(viewId);
		}
		
		this.previousView = function()
		{
			var lastView = this.viewHistory.pop();
			if(lastView)
			{
				this.showView(this.allViews[lastView], false);
				if(this.allViews[lastView].viewId == 'process-panel-view')
				{
					queueViewManager.reloadCurrentQueue();
				}
			}
		}
		
		this.showLoadingScreen = function()
		{
			this.showView(this.allViews['loading-screen'], true);
		}
		
		this.showQueueList = function()
		{
			this.showView(this.allViews['outer-queues'], true);
		}
		
		
		this.showConfiguration = function()
		{
			this.showView(this.allViews['configuration'], true);
		}
		
		this.showSearchProcessPanel = function()
		{
			this.showView(this.allViews['search-view'], true);
		}
		
		this.showNewProcessPanel = function()
		{
			this.showView(this.allViews['new-process-view'], true);
		};
		
		this.showProcessList = function()
		{
			this.showView(this.allViews['process-panel-view'], true);
		}
		
		this.showProcessData = function()
		{
			this.showView(this.allViews['process-data-view'], true);
			$('#actions-list').fadeIn(600);
		}
		
		this.hasPreviousView = function()
		{
			return this.viewHistory.length > 0;
		}
		
		this.addError = function(errorMessage)
		{
			if($("#error-screen").is(":visible") == false)
			{
				$("#error-screen").fadeIn(500);
				$("#loading-screen").hide();
			}
			
			$('#error-screen').append('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button>'+errorMessage+'</div>')
		}
		
		this.clearErrors = function()
		{
			$('#error-screen').empty();
		}
		
		
		

		
		this.showView = function(windowView, addToHistory)
		{
			console.log("windowView: "+windowView); 
			$(document.getElementById(this.currentView)).stop(true, true);
			
			if(this.tabletMode == true && $("#mobile-collapse").hasClass('in') == true)
			{
				$("#mobile-collapse").collapse('hide');
			}
			windowManager.clearProcessView();
			
			$.each(this.allViews, function(index, view ) 
			{ 
				if(this != windowView.viewId)
				{
					$(document.getElementById(view.viewId)).hide();
				}
			});
			
			if("loading-screen" != this.currentView)
			{
				this.viewHistory.push(this.currentView);
			}
			
			this.currentView = windowView.viewId;
			$(document.getElementById(windowView.viewId)).fadeIn(500);
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
				
				
				$.ajax(url)
				.done(function() 
				{
					widgetToClose.remove();
				});
				
			});
			
			vaadinWidgetsCount = 0;
			vaadinWidgetsLoadedCount = 0;
		}
	}
  

	

 
	
 
 </script>