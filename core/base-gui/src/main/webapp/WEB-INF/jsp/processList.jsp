<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="process-panel" id="process-panel-view">
	<table id="processesTable" class="process-table" border="1">
		<thead>
			<tr>
				<th style="width:30%;"><spring:message code="processes.list.table.process.name" /></th>
				<th style="width:20%;"><spring:message code="processes.list.table.process.code" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.creator" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.assignee" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.creationdate" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.deadline" /></th>
			</tr>
		</thead>
		<tbody></tbody>
	</table>
</div>

<script type="text/javascript">
//<![CDATA[
$(document).ready(function()
{
	loadQueue('');
});

function reloadQueue(newQueueName, queueType)
{
	if ($('#process-panel-view').css("visibility") == "hidden") 
	{
		$('#process-panel-view').show();
	}
	else
	{
		var requestUrl = '<spring:url value="/processes/loadProcessesList.json?queueName="/>' + newQueueName + '&queueType=' + queueType;

		$('#processesTable').dataTable().fnReloadAjax(requestUrl);
		
		showProcessList();
	}
}


function loadQueue() 
{
	var columnDefs = [
						 { "sName":"name", "bSortable": true,"mData": function(object){return generateNameColumn(object);}},
						 { "sName":"code", "bSortable": true,"mData": "code" },
						 { "sName":"creator", "bSortable": true,"mData": "creator" },
						 { "sName":"assignee", "bSortable": true,"mData": "assignee" },
						 { "sName":"creationDate", "bSortable": true,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm');}},
						 { "sName":"deadline", "bSortable": true,"mData": function(object){return object.deadline == null ? "" : $.format.date(object.deadline, 'dd-MM-yyyy, HH:mm');}}
					 ];

	var requestUrl = '<spring:url value="/processes/loadProcessesList.json?queueName=activity.assigned.tasks&queueType=process"/>';
	createDataTable('processesTable',requestUrl,columnDefs);
	
}

	function generateNameColumn(task)
	{
	    var linkBody = '<a class="process-view-link" data-toggle="tooltip" title="'+task.tooltip+'" onclick="loadProcessView('+task.processStateConfigurationId+','+task.taskId+') ">' + task.processName + '</a>';

        return linkBody;
    }
	
	function loadProcessView(processStateConfigurationId, taskId)
	{
		clearProcessView();
		var widgetJson = $.post('<spring:url value="/task/loadTask"/>', 
		{
			"processStateConfigurationId": processStateConfigurationId,
			"taskId": taskId
		}, function(data) 
		{ 
			console.log( "data: "+data );
			clearAlerts();
			$('#process-data-view').empty();
			console.log( "data: "+data );
			$("#process-data-view").append(data);
			
			showProcessData();
		});
	}
	
	function loadProcessView2(processStateConfigurationId, taskId)
	{
		clearProcessView();
		
		var widgetJson = $.getJSON('<spring:url value="/processes/loadProcessWidgets.json"/>', 
		{
			"processStateConfigurationId": processStateConfigurationId
		})
		.done(function(data) 
		{ 
			clearAlerts();
			$('#vaadin-widgets').empty();
			
			$.each( data, function( ) 
			{
				appendWidget(this, "#vaadin-widgets", taskId);
			});
			
			showProcessData();
		})
		.fail(function() { console.log( "error" ); })
		.always(function() { console.log( "complete" ); });
		
		var actionsJson = $.getJSON('<spring:url value="/processes/loadProcessActions.json"/>', 
		{
			"processStateConfigurationId": processStateConfigurationId
		})
		.done(function(data) 
		{ 
			$('#actions-list').empty();
			
			$.each( data, function( ) 
			{
				appendAction(this, "#actions-list", taskId);
			});
			
			appendSaveAction("#actions-list", taskId);
			appendCancelAction("#actions-list", taskId);
		})
		.fail(function() { console.log( "error" ); })
		.always(function() { console.log( "complete" ); });

	}
//]]>
</script>