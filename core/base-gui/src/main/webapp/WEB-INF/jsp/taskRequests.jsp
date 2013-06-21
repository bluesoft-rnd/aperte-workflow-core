<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="search-view">
	<div class="process-queue-name">
		<spring:message code="searching.view.header" />
	</div>
	<div id="search-process-table">
		<input type="text" id="search-expression-text" class="input-medium" placeholder="<spring:message code='processes.search.textarea.input' />">
	</div> 
	<table id="searchTable" class="search-table" border="1">
		<thead>
			<tr>
				<th style="width:30%;"><spring:message code="processes.list.table.process.name" /></th>
				<th style="width:20%;"><spring:message code="processes.list.table.process.code" /></th>
				<th style="width:10%;"><spring:message code="processes.list.table.process.assignee" /></th>
				<th style="width:15%;"><spring:message code="processes.list.table.process.creationdate" /></th>
			</tr>
		</thead>
		<tbody></tbody>
	</table>
</div>
 <script type="text/javascript">

	var isTableLoaded = false;
	$(document).ready(function()
	{
		$('#search-view').hide();
	});
	
	var delay = (function(){
	  var timer = 0;
	  return function(callback, ms){
		clearTimeout (timer);
		timer = setTimeout(callback, ms);
	  };
	})();
	
	$('#search-expression-text').keyup(function() 
	{
		delay(function()
		{
		  if(isTableLoaded == false)
		  {
				isTableLoaded = true;
				loadSearchTable();
		  }
		  else
		  {
				var requestUrl = '<spring:url value="/processes/searchTasks.json?sSearch="/>'+$('#search-expression-text').val() ;

				$('#searchTable').dataTable().fnReloadAjax(requestUrl);
		  }
		}, 1000 );
	});
	
	function loadSearchTable() 
	{
		var columnDefs = [
							 { "sName":"name", "bSortable": true,"mData": function(object){return generateNameColumn(object);}},
							 { "sName":"code", "bSortable": true, "mData": "code" },
							 { "sName":"assignee", "bSortable": true,"mData": "assignee" },
							 { "sName":"creationDate", "bSortable": true,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm');}}
						 ];

		var requestUrl = '<spring:url value="/processes/searchTasks.json?sSearch="/>'+$('#search-expression-text').val();
		createDataTable('searchTable',requestUrl,columnDefs);
	}
 

 
 </script>
