﻿<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="process-panel" id="process-panel-view" hidden="true">
	<div class="process-queue-name" id="process-queue-name-id">
		 
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

	function QueueView(tableObject, viewName)
	{
		this.tableObject = tableObject;
		this.viewName = viewName;
	}
	
	function QueueViewManager()
	{
		this.views = {};

		this.currentQueue = 'activity.created.assigned.tasks';
		this.currentQueueType = 'process';
		this.currentOwnerLogin = '${aperteUser.login}';
		this.currentQueueDesc = '<spring:message code="activity.assigned.tasks" />';
		
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
			
			var requestUrl = '<spring:url value="/processes/loadProcessesList.json?queueName="/>' + newQueueName + '&queueType=' + queueType + '&ownerLogin='+ownerLogin;
			
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

  
	function AperteDataTable(tableId, columnDefs, sortingOrder)
	{
		this.tableId = tableId;
		this.requestUrl = '';
		this.columnDefs = columnDefs;
		this.sortingOrder = sortingOrder;
		this.dataTable;
		
		this.initialized = false;
		
		this.reloadTable = function(requestUrl)
		{
			
			this.requestUrl = requestUrl;
			if(this.initialized == false)
			{
				this.createDataTable();
				this.initialized = true;
			}
			else
			{
				this.dataTable.fnReloadAjax(this.requestUrl);
			}
		}
		
		this.enableMobileMode = function()
		{
		}
		
		this.enableTabletMode = function()
		{
		}
		
		this.disableMobileMode = function()
		{
		}
		
		this.disableTabletMode = function()
		{
		}
		
		this.createDataTable = function()
		{
			this.dataTable = $('#'+this.tableId).dataTable({
				"bLengthChange": true,
				"bFilter": true,
				"bProcessing": true,
				"bServerSide": true,
				"bInfo": true,
				"aaSorting": sortingOrder,
				"bSort": true,
				"iDisplayLength": 10,
				"bStateSave": true,
				"sDom": 'R<"top"t><"bottom"plr>',
				"sAjaxSource": this.requestUrl,
				"fnServerData": function ( sSource, aoData, fnCallback ) {

					$.ajax( {
						"dataType": 'json',
						"type": "POST",
						"url": sSource,
						"data": aoData,
						"success": fnCallback
					} );
				},
				"aoColumns": this.columnDefs,
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
			
			if(windowManager.mobileMode == true)
			{
				this.enableMobileMode();
			}
			
			if(windowManager.tabletMode == true)
			{
				this.enableTabletMode();
			}
		}
		
		this.toggleColumnButton = function(columnName, active)
		{
			
			var checkbox = $("#button-"+this.tableId+'-'+columnName);
			
			var changeState = !XOR(checkbox.is(':checked'), active); 
			if(changeState == true)
			{
				checkbox.trigger('click');
			}

		}
	
		this.toggleColumn = function(columnName)
		{
			var dataTable = this.dataTable;
			$.each(dataTable.fnSettings().aoColumns, function (columnIndex, column) 
			{
				if (column.sName == columnName)
				{
					  dataTable.fnSetColumnVis(columnIndex, column.bVisible ? false : true, false);
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