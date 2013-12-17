<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ page import="pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig" %>
<%@ page import="pl.net.bluesoft.rnd.processtool.ui.jsp.SearchView" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<div id="search-view" hidden="true" class="search-view">
	<div class="process-queue-name">
		<spring:message code="searching.view.header" />
	</div>
	<div id="search-process-table">
		<input type="text" id="search-expression-text" class="input-medium" placeholder="<spring:message code='processes.search.textarea.input' />">
		<select id="search-process-type" class="search-process-type">
			<%
			SearchView.sortByLocalizedDescription(processStartList, request.getLocale());
			%>
			<c:forEach var="processStart" items="${processStartList}">
				<option value="${processStart.bpmDefinitionKey}"><spring:message code="${processStart.processName}" /></option>
			</c:forEach>
        </select>
	</div> 
	<div class="search-button" id="search-process-button" onClick="searchProcess();">
		<spring:message code="processes.search.button.label" />
	</div>
	<table id="searchTable" class="search-table table table-striped" border="1">
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
 
 	$(document).ready(function()
	{
		windowManager.addView("search-view");
	});

	var isTableLoaded = false;
	
	var delay = (function(){
	  var timer = 0;
	  return function(callback, ms){
		clearTimeout (timer);
		timer = setTimeout(callback, ms);
	  };
	})();
	
	function searchProcess()
	{
		   if(isTableLoaded == false)
		  {
				isTableLoaded = true;
				loadSearchTable();
		  }
		  else
		  {

				var requestUrl = '<portlet:resourceURL id="searchTasks"/>';
				requestUrl += "&<portlet:namespace/>sSearch=" + $('#search-expression-text').val();
				requestUrl += "&<portlet:namespace/>processKey="+$('#search-process-type').val();

				$('#searchTable').dataTable().fnReloadAjax(requestUrl);
		  }
	}
	
	function loadSearchTable() 
	{
		 
		var columnDefs = [
							 { "sName":"name", "bSortable": true,"mData": function(object){return generateNameColumn(object);}},
							 { "sName":"code", "bSortable": true, "mData": "code" },
							 { "sName":"assignee", "bSortable": true,"mData": "assignee" },
							 { "sName":"creationDate", "bSortable": true,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm');}}
						 ];

		var requestUrl = '<spring:url value="/processes/searchTasks.json?sSearch="/>'+$('#search-expression-text').val();
		createSearchDataTable('searchTable',requestUrl,columnDefs,[[ 0, "asc" ]]);
	}
	
			
	function createSearchDataTable(tableId,url,columns,sortingOrder){
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
 

 
 </script>
