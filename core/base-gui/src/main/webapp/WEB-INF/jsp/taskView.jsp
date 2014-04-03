﻿<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<div class="process-panel" id="process-panel-view" hidden="true">
	<div class="process-queue-name apw_highlight" id="process-queue-name-id">
		 
	</div>
	
	<%@include file="processList.jsp" %>

	<%@include file="customQueueList.jsp" %>
	
	<div id="search-process-table">
		<input type="text" id="processInputTextField" class="input-medium" placeholder="<spring:message code='processes.search.label' />">
	</div> 

</div>

<script type="text/javascript">
//<![CDATA[
  	var queueViewManager = new QueueViewManager();
	
	$(document).ready(function()
	{
		windowManager.addView("process-panel-view");
	});
	
	function loadCookies(queueName){
		var selectedProcess = $.cookie("pl.net.aperteworkflow."+queueName+"Config");
		if (!selectedProcess){
			selectedProcess = selectAllCheckboxes(queueName);
			saveProcessCookie(selectedProcess,queueName);
		}
	return selectedProcess
	}
	
	function selectAllCheckboxes(viewName){
		var list = $('input:checkbox[name='+viewName+']').map(function() {
			return '"'+$(this).val() +'":'+ true;
		}).get().join();
		list = "{"+list+"}";
		return list;
	}
	
	function saveProcessCookie(selectedProcess,queueName){
		$.cookie("pl.net.aperteworkflow."+queueName+"Config",selectedProcess,{ expires: 365 });
	}
	
	function parseCookie(loadedCookie){
		return jQuery.parseJSON(loadedCookie);
	}

	function QueueView(tableObject, viewName)
	{
		this.tableObject = tableObject;
		this.viewName = viewName;
	}
	
	function QueueViewManager()
	{
		this.views = {};

		this.currentQueue = 'activity.created.all.tasks';
		this.currentQueueType = 'process';
		this.currentOwnerLogin = '${aperteUser.login}';
		this.currentQueueDesc = '<spring:message code="activity.created.all.tasks" />';
		
		this.loadQueue = function(newQueueName, queueType, ownerLogin, queueDesc)
		{
			var oldView = this.views[this.currentQueueType];
			var newView = this.views[queueType];
			
			$('#'+oldView.viewName).hide();
			$('#'+newView.viewName).show();
			
			
			this.currentQueue = newQueueName;
			this.currentQueueType = queueType;
			this.currentOwnerLogin = ownerLogin;
			this.currentQueueDesc = queueDesc;

			var requestUrl = '<portlet:resourceURL id="loadProcessesList"/>';
			requestUrl += "&<portlet:namespace/>queueName=" + newQueueName;
			requestUrl += "&<portlet:namespace/>queueType=" + queueType;
			requestUrl += "&<portlet:namespace/>ownerLogin=" + ownerLogin;
			
			newView.tableObject.reloadTable(requestUrl);
			
			windowManager.showProcessList();
			
			$("#process-queue-name-id").text('<spring:message code="processes.currentqueue" />'+" "+queueDesc);
		}
		
		this.reloadCurrentQueue = function()
		{
			this.loadQueue(this.currentQueue, this.currentQueueType, this.currentOwnerLogin, this.currentQueueDesc);
		}
		
		this.addTableView = function(queueType, tableObject, viewName)
		{
			this.views[queueType] = new QueueView(tableObject, viewName);
		}
		
		this.toggleColumn = function(viewName, columnName)
		{
			this.views[viewName].tableObject.toggleColumn(columnName);
		}
		
		this.enableMobileMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.enableMobileMode();
				}
			});
		}
		
		this.enableTabletMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.enableTabletMode();
				}
			});
		}
		
		this.disableMobileMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.disableMobileMode();
				}
			});
		}
		
		this.disableTabletMode = function()
		{
			$.each(this.views, function(viewName, view)
			{
				if(view.tableObject.initialized == true)
				{
					view.tableObject.disableTabletMode();
				}
			});
		}

		
		
	}

	$('#processInputTextField').keyup(function() 
	{
		
		delay(function(){
		  $('#processesTable').dataTable().fnFilter( $('#processInputTextField').val() );
		}, 500 );
	});

//]]>
</script>