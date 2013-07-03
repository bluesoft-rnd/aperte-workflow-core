<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="network-requests" class="process-panel" hidden="true">
	<div class="process-queue-name">
		<spring:message code="accidents.table.header" />
	</div>
	<table id="request-table" class="search-table table table-striped" border="1">
		<thead>
			<tr>
				<th style="width:10%;"><spring:message code="accidents.table.number" /></th>
				<th style="width:10%;"><spring:message code="accidents.table.priority" /></th>
				<th style="width:10%;"><spring:message code="accidents.table.status" /></th>
				<th style="width:15%;"><spring:message code="accidents.table.address" /></th>
				<th style="width:30%;"><spring:message code="accidents.table.description" /></th>
				<th style="width:15%;"><spring:message code="accidents.table.date" /></th>
				<th style="width:10%;"><spring:message code="accidents.table.actions" /></th>
			</tr>
		</thead>
		<tbody></tbody>
	</table>
</div>
 <script type="text/javascript">

	var isTableLoaded = false;
	
	var delay = (function(){
	  var timer = 0;
	  return function(callback, ms){
		clearTimeout (timer);
		timer = setTimeout(callback, ms);
	  };
	})();
	
	
	function reloadAccidents()
	{
			if(isTableLoaded == false)
		  {
				isTableLoaded = true;
				loadAccidentsTable();
		  }
		  else
		  {
				var requestUrl = '<spring:url value="/dispatcher/mpwikcontroler/getAwaitingTasks"/>';

				$('#request-table').dataTable().fnReloadAjax(requestUrl);
		  }
	}
	
	function loadAccidentsTable() 
	{
		var columnDefs = [
							 { "sName":"number", "bSortable": true,"mData": "number"},
							 { "sName":"priority", "bSortable": true, "mData": function(object){return translateAccidentPriority(object.priority)} },
							 { "sName":"status", "bSortable": true, "mData": function(object){return translateAccidentStatus(object.status)} },
							 { "sName":"address", "bSortable": true, "mData": "address" },
							 { "sName":"description", "bSortable": true,"mData": "description" },
							 { "sName":"date", "bSortable": true,"mData": function(object){return $.format.date(object.date, 'dd-mm-yyyy, HH:mm');}},
							 { "sName":"actions", "bSortable": false,"mData": function(object)
								{
									return claimAccidentButton(object.number, object.priority, 
											object.description, object.type, object.address, $.format.date(object.date, 'dd-mm-yyyy, HH:mm')); 
									
								}},
						 ];

		var requestUrl = '<spring:url value="/dispatcher/mpwikcontroler/getAwaitingTasks"/>';
		createDataTable('request-table',requestUrl,columnDefs, [[ 0, "desc" ]]);
	}
	
	function claimAccidentButton(accidentNumber, accidentPriority, accidentBody, accidentType, accidentAddress, accidentStartDate)
	{
		
		var address = accidentAddress.replace(/\r/,"").replace(/\n/,""); 
		return '<button id="claim-accident-button-login" type="button" class="btn login-button" onClick="claimAccident(\''+accidentNumber+
		'\', \''+accidentPriority+'\', \'ZAR\', \''+accidentBody+'\',  \''+accidentType+'\', \''+address+'\',\''+accidentStartDate+'\')" ><spring:message code="accidents.claim.accident" /></button>';
	}
	
	function translateAccidentStatus(accidentStatus)
	{
		switch(accidentStatus)
		{
			case "REGISTERED": return '<spring:message code="accidents.status.registered" />';
			case "RECOGNITION": return '<spring:message code="accidents.status.recognition" />';
			case "DIAGNOSTICS": return '<spring:message code="accidents.status.diagnostistcs" />';
			case "TOTHENETWORK": return '<spring:message code="accidents.status.tothenetwork" />';
			case "ASSIGNED": return '<spring:message code="accidents.status.assigned" />';
			case "CLOSED": return '<spring:message code="accidents.status.closed" />';
			case "STARTED": return '<spring:message code="accidents.status.started" />';
			case "RECONSTRUCTION": return '<spring:message code="accidents.status.reconstruction" />';
			case "FINSHED": return '<spring:message code="accidents.status.finished" />';
			case "PLANNED": return '<spring:message code="accidents.status.planned" />';
		}
	}
 
	function translateAccidentPriority(accidentPriority)
	{
		switch(accidentPriority)
		{
			case "HIGH": return '<spring:message code="accidents.priority.high" />';
			case "MEDIUM": return '<spring:message code="accidents.priority.medium" />';
			case "LOW": return '<spring:message code="accidents.priority.low" />';
		}
	}
	
	function claimAccident(accidentNumber, accidentPriority, accidentStatus, accidentBody, accidentType, accidentAddress, accidentStartDate)
	{
		var simpleProperties = 
		{
			"accidentNumber": accidentNumber,
			"accidentPriority": accidentPriority,
			"accidentStatus": accidentStatus,
			"accidentBody": accidentBody,
			"accidentType": accidentType,
			"accidentAddress": accidentAddress,
			"accidentStartDate": accidentStartDate,
			"externalKey": accidentNumber
		};
		startProcess('accident_handling', simpleProperties);
	}
 
 </script>
