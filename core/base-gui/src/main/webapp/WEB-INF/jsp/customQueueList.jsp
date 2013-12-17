﻿<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<div class="process-panel" id="customqueue-panel-view" hidden="true">
	<table id="customQueueTable" class="process-table table table-striped" border="1">
		<thead>
			<tr>
				<th style="width:15%;"><spring:message code="processes.list.table.process.name" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.step" /></th>
				<th style="width:12%;"><spring:message code="processes.list.table.process.code" /></th>
				<th style="width:9%;"><spring:message code="processes.list.table.process.creator" /></th>
				<th style="width:9%;"><spring:message code="processes.list.table.process.creationdate" /></th>
				<th style="width:9%;"><spring:message code="processes.list.table.process.deadline" /></th>
				<th style="width:12%;"><spring:message code="processes.list.table.process.stepinfo" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.actions" /></th>
			</tr>
		</thead>
		<tbody></tbody>
	</table>

</div>

<script type="text/javascript">
//<![CDATA[
  	$(document).ready(function()
	{
		var loadedCustom = loadCookies("queue");
		var parsedCustom = parseCookie(loadedCustom);
	
		var dataTable = new AperteDataTable("customQueueTable", 
			[
				 { "sName":"name", "bSortable": true,"bVisible":parsedCustom.name,"mData": function(object){return generateNameColumn(object);}},
				 { "sName":"step", "bSortable": true,"bVisible":parsedCustom.step, "mData": "step" },
				 { "sName":"code", "bSortable": true,"bVisible":parsedCustom.code, "mData": "code" },
				 { "sName":"creator", "bSortable": true,"bVisible":parsedCustom.creator,"mData": "creator" },
				 { "sName":"creationDate", "bSortable": true,"bVisible":parsedCustom.creationDate,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm');}},
				 { "sName":"deadline", "bSortable": true,"bVisible":parsedCustom.deadline,"mData": function(object){return object.deadline == null ? "<spring:message code='processes.list.table.nodeadline' />" : $.format.date(object.deadline, 'dd-MM-yyyy, HH:mm');}},
				 { "sName":"stepInfo", "bSortable": true ,"bVisible":parsedCustom.stepInfo, "mData":"stepInfo" },
				 { "sName":"actions", "bSortable": false,"bVisible":parsedCustom.actions,"mData": function(object){return generateButtons(object)}},
			 ],
			 [[ 5, "desc" ]]
			);
			
		queueViewManager.addTableView('queue', dataTable, 'customqueue-panel-view');
			
		dataTable.enableMobileMode = function()
		{
			this.toggleColumnButton("deadline", false);
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
			this.toggleColumnButton("creationDate", true);
		}
		
		dataTable.disableTabletMode = function()
		{
			this.toggleColumnButton("creator", true);
			this.toggleColumnButton("code", true);
		}
	});
	
	function generateButtons(task)
	{
		var linkBody = '';
		if(task.queueName)
		{
			linkBody += '<button id="link-'+task.queueName+'" class="btn aperte-button aperte-button-hide" type="button" data-toggle="tooltip" title="<spring:message code="activity.tasks.task-claim-details" />" onclick="claimTaskFromQueue(this, \''+task.queueName+'\','+task.processStateConfigurationId+','+task.taskId+'); "><spring:message code="activity.tasks.task-claim" /></a>';
		}
		
		return linkBody;
	}
	
	function claimTaskFromQueue(button, queueName, processStateConfigurationId, taskId)
	{
		$(button).prop('disabled', true);
		windowManager.showLoadingScreen();
		
		var bpmJson = $.post('<portlet:resourceURL id="claimTaskFromQueue"/>',
		{
			"queueName": queueName,
			"taskId": taskId,
			"userId": queueViewManager.currentOwnerLogin
		}, function(result)
		{
		    var newTask = result.newTask;
			clearAlerts();
			console.log( "task claimed, new task: "+newTask.taskId); 
			reloadQueues();
			loadProcessView(processStateConfigurationId, newTask.taskId);
		})
		.fail(function(request, status, error) 
		{	
			console.log( "ojoj:  "+error); 
		});
	}
//]]>
</script>