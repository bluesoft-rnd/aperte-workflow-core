﻿<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="process-tasks-view" id="task-view-processes" hidden="true">
	<table id="processesTable" class="process-table table table-striped" border="1">
		<thead>

				<th style="width:15%;"><spring:message code="processes.list.table.process.name" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.step" /></th>
				<th style="width:20%;"><spring:message code="processes.list.table.process.code" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.creator" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.assignee" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.creationdate" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.deadline" /></th>

		</thead>
		<tbody></tbody>
	</table>
</div>


<script type="text/javascript">
//<![CDATA[

  	$(document).ready(function()
	{
		
		var dataTable = new AperteDataTable("processesTable", 
			[
				 { "sName":"name", "bSortable": true,"mData": function(object){return generateNameColumn(object);}},
				 { "sName":"step", "bSortable": true, "mData": "step" },
				 { "sName":"code", "bSortable": true, "mData": "code" },
				 { "sName":"creator", "bSortable": true,"mData": "creator" },
				 { "sName":"assignee", "bSortable": true,"mData": "assignee" },
				 { "sName":"creationDate", "bSortable": true,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm');}},
				 { "sName":"deadline", "bSortable": true,"mData": function(object){return object.deadline == null ? "<spring:message code='processes.list.table.nodeadline' />" : $.format.date(object.deadline, 'dd-MM-yyyy, HH:mm');}}
			 ],
			 [[ 5, "desc" ]]
			);
		
		dataTable.enableMobileMode = function()
		{
			this.toggleColumnButton("deadline", false);
			this.toggleColumnButton("assignee", false);
			this.toggleColumnButton("creationDate", false);
		}
		
		dataTable.enableTabletMode = function()
		{
			this.toggleColumnButton("creator", false);
			this.toggleColumnButton("code", false);
		}
		
		dataTable.disableMobileMode = function()
		{
			this.toggleColumnButton("deadline", true);
			this.toggleColumnButton("assignee", true);
			this.toggleColumnButton("creationDate", true);
		}
		
		dataTable.disableTabletMode = function()
		{
			this.toggleColumnButton("creator", true);
			this.toggleColumnButton("code", true);
		}
		
		queueViewManager.addTableView('process', dataTable, 'task-view-processes');
	});

	

	function generateNameColumn(task)
	{
		var showOnClickCode = 'onclick="loadProcessView('+task.processStateConfigurationId+','+task.taskId+')"';
		
	    var linkBody = '<a class="process-view-link" data-toggle="tooltip" title="'+task.tooltip+'" '+showOnClickCode+' ">' + task.processName + '</a>';

        return linkBody;
    }
	
	
	function loadProcessView(processStateConfigurationId, taskId)
	{
		windowManager.clearProcessView();
		windowManager.showLoadingScreen();
		var widgetJson = $.post('<spring:url value="/task/loadTask"/>', 
		{
			"processStateConfigurationId": processStateConfigurationId,
			"taskId": taskId
		}, function(data) 
		{ 
			
			clearAlerts();
			windowManager.showProcessData();
			$('#process-data-view').empty();
			$("#process-data-view").append(data);
			checkIfViewIsLoaded();
			
		});
	}
	


//]]>
</script>