<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="process-panel" id="process-panel-view" hidden="true">
	<div class="process-queue-name" id="process-queue-name-id">
		 
	</div>
	<table id="processesTable" class="process-table table table-striped" border="1">
		<thead>
			<tr>
				<th style="width:15%;"><spring:message code="processes.list.table.process.name" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.step" /></th>
				<th style="width:20%;"><spring:message code="processes.list.table.process.code" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.creator" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.assignee" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.creationdate" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.deadline" /></th>
			</tr>
		</thead>
		<tbody></tbody>
	</table>
	<div id="search-process-table">
		<input type="text" id="processInputTextField" class="input-medium" placeholder="<spring:message code='processes.search.label' />">
	</div> 

</div>

<script type="text/javascript">
//<![CDATA[

	$('#processInputTextField').keyup(function() 
	{
		
		delay(function(){
		  $('#processesTable').dataTable().fnFilter( $('#processInputTextField').val() );
		}, 500 );
	});

	var currentQueue = 'activity.assigned.tasks';
	var currentQueueType = 'process';
	var currentOwnerLogin = '${aperteUser.login}';
	var currentQueueDesc = '<spring:message code="activity.assigned.tasks" />';
	
	function reloadCurrentQueue()
	{
		reloadQueue(currentQueue, currentQueueType, currentOwnerLogin, currentQueueDesc);
	}
	
	function toggleColumnButton(columnNumber, active)
	{
		
		var button = $("#process-table-hide-"+columnNumber);
		
		var changeState = !XOR(button.hasClass("active"), active); 
		if(changeState == true)
		{
			button.trigger('click');
		}

	}
	
	function toggleColumn(columnNumber)
	{
		var oTable = $('#processesTable').dataTable();
		var bVis = oTable.fnSettings().aoColumns[columnNumber].bVisible;
		oTable.fnSetColumnVis( columnNumber, bVis ? false : true);
	}
	

	function reloadQueue(newQueueName, queueType, ownerLogin, queueDesc)
	{	
		currentQueue = newQueueName;
		currentQueueType = queueType;
		currentOwnerLogin = ownerLogin;
		currentQueueDesc = queueDesc;
		
		if ($('#process-panel-view').css("visibility") == "hidden") 
		{
			$('#process-panel-view').show();
		}
		else
		{
			var requestUrl = '<spring:url value="/processes/loadProcessesList.json?queueName="/>' + newQueueName + '&queueType=' + queueType + '&ownerLogin='+ownerLogin;

			$('#processesTable').dataTable().fnReloadAjax(requestUrl);
			
			windowManager.showProcessList();
			
			$("#process-queue-name-id").text('<spring:message code="processes.currentqueue" />'+" "+currentQueueDesc);
		}
		
		
	}


	function loadQueue() 
	{
		var columnDefs = [
							 { "sName":"name", "bSortable": true,"mData": function(object){return generateNameColumn(object);}},
							 { "sName":"step", "bSortable": true, "mData": "step" },
							 { "sName":"code", "bSortable": true, "mData": "code" },
							 { "sName":"creator", "bSortable": true,"mData": "creator" },
							 { "sName":"assignee", "bSortable": true,"mData": "assignee" },
							 { "sName":"creationDate", "bSortable": true,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm');}},
							 { "sName":"deadline", "bSortable": true,"mData": function(object){return object.deadline == null ? "" : $.format.date(object.deadline, 'dd-MM-yyyy, HH:mm');}}
						 ];

		var requestUrl = '<spring:url value="/processes/loadProcessesList.json?queueName=activity.assigned.tasks&queueType=process"/>';
		createDataTable('processesTable',requestUrl,columnDefs, [[ 5, "desc" ]]);
	
		$("#process-queue-name-id").text('<spring:message code="processes.currentqueue" />'+" "+currentQueueDesc);
	}

	function generateNameColumn(task)
	{
		
	    var linkBody = '<a class="process-view-link" data-toggle="tooltip" title="'+task.tooltip+'" onclick="loadProcessView('+task.processStateConfigurationId+','+task.taskId+') ">' + task.processName + '</a>';

		if(task.queueName)
		{
			linkBody += ' || <a id="link-'+task.queueName+'" class="queue-task-assign-link" data-toggle="tooltip" title="<spring:message code="activity.tasks.task-claim" />" onclick="claimTaskFromQueue(\''+task.queueName+'\','+task.processStateConfigurationId+','+task.taskId+'); "><spring:message code="activity.tasks.task-claim" /></a>'
		}
        return linkBody;
    }
	
	function claimTaskFromQueue(queueName, processStateConfigurationId, taskId)
	{
		
		var bpmJson = $.post('<spring:url value="/task/claimTaskFromQueue"/>', 
		{
			"queueName": queueName,
			"taskId": taskId
		}, function(newTask) 
		{ 
			console.log( "task claimed, new task: "+newTask.taskId); 
			reloadQueues();
			loadProcessView(processStateConfigurationId, newTask.taskId);
		})
		.fail(function(request, status, error) 
		{	
			console.log( "ojoj:  "+error); 
		});
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
	

	function createDataTable(tableId,url,columns,sortingOrder){
		$('#'+tableId).dataTable({
			"bLengthChange": true,
			"bFilter": true,
			"bProcessing": true,
			"bServerSide": true,
			"bInfo": true,
			"aaSorting": sortingOrder,
			"bSort": true,
			"iDisplayLength": 10,
			"sDom": '<"top"t><"bottom"plr>',
			"sAjaxSource": url,
			"fnServerData": function ( sSource, aoData, fnCallback ) {

				$.ajax( {
					"dataType": 'json',
					"type": "POST",
					"url": sSource,
					"data": aoData,
					"success": fnCallback
				} );
			},
			"aoColumns": columns,
			"oLanguage": {
				  //todo: uzeleznic tresci od tlumaczen w messages
				  "sInfo": "Wyniki od _START_ do _END_ z _TOTAL_",
				  "sEmptyTable": "<spring:message code='datatable.empty' />",
				  "sInfoEmpty": "<spring:message code='datatable.empty' />",
				  "sProcessing": "<spring:message code='datatable.processing' />",
			      "sLengthMenu": "<spring:message code='datatable.records' />",			  
				  "sInfoFiltered": "",
				  "oPaginate": {
					"sFirst": "<spring:message code='datatable.paginate.firstpage' />",
					"sNext": "<spring:message code='datatable.paginate.next' />",
					"sPrevious": "<spring:message code='datatable.paginate.previous' />"
				  }

				}
		});
	}
//]]>
</script>