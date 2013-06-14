<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="left-menu">
	<div class="start-process-button" id="process-start-button">
		<spring:message code="processes.start.new.process" />
	</div>
	<div class="queues-list" id="queue-view-block">
		<c:forEach var="userQueue" items="${queues}">
				<c:if test="${userQueue.userLogin==aperteUser.login}">
					User: ${userQueue.userLogin}
					 
				</c:if> 
				<c:if test="${userQueue.userLogin!=aperteUser.login}">
					 User:   ${userQueue.userLogin}
				</c:if> 
				
				<c:forEach var="queue" items="${userQueue.processesList}">
					<div class="queue-list-row-process">
						<div id="${queue.queueId}" class="queue-list-name"><a class="queue-list-link" onclick="reloadQueue('${queue.queueName}', 'process') ">${queue.queueDesc}</a></div>
						<div class="queue-list-size">${queue.queueSize}</div>
						<br style="clear: left;" />
					</div>
				</c:forEach>

				<c:forEach var="queue" items="${userQueue.queuesList}">
					<div class="queue-list-row-queue">
						<div id="${queue.queueId}" class="queue-list-name"><a class="queue-list-link" onclick="reloadQueue('${queue.queueName}', 'queue') ">${queue.queueDesc}</a></div>
						<div class="queue-list-size">${queue.queueSize}</div>
						<br style="clear: left;" />
					</div>
				</c:forEach>
		 </c:forEach>
	</div>
</div>
 <script type="text/javascript">
 
	$(document).ready(function()
	{
		$('#new-process-view').hide();
		reloadQueues();
	});
 
	$("#process-start-button").click(
	  function () 
	  {
		showNewProcessPanel();
	  }
	);
	
	function reloadQueues()
	{
		console.log( "reload queues: " );
		var queuesJson = $.getJSON('<spring:url value="/queues/getUserQueues.json"/>', function(queues) 
		{ 
			console.log( "queues: "+queues );
			$('#queue-view-block').empty();
	
			
			$.each( queues, function( ) 
			{
				var accordionID = 'accordion-list-'+this.userLogin;
				$( "<a>", { text: "User: "+this.userLogin, "data-toggle":"collapse", "data-parent":'#queue-view-block', href:"#"+accordionID} )
				.appendTo( '#queue-view-block' );
				
				$( "<div>", { id : accordionID, "class": "accordion-body collapse"} )
				.appendTo( '#queue-view-block' );
				
				$.each( this.processesList, function( ) 
				{
					addProcessRow(this, accordionID);
				});
				
				$.each( this.queuesList, function( ) 
				{
					addProcessRow(this, accordionID);
				});
			});
		});
	}
	
	function addProcessRow(processRow, accordionID)
	{
		var layoutId = 'queue-view-' + processRow.queueId;
		var innerDivId = ''+processRow.queueId;

		$( "<div>", { id : layoutId, "class": "queue-list-row-queue"} )
		.appendTo( '#'+accordionID );
		
		$( "<div>", { id : innerDivId, "class": "queue-list-name"} )
		.appendTo( '#'+layoutId );
		
		$( "<a>", { id : 'link-'+processRow.queueId, "class": "queue-list-link", text: processRow.queueDesc, "onclick":"reloadQueue('"+processRow.queueName+"', 'process') "} )
		.appendTo( '#'+innerDivId );
		
		$( "<div>", { "class": "queue-list-size", text: processRow.queueSize} )
		.appendTo( '#'+layoutId );
		
		$( "<br>", { style: "clear: left;"} )
		.appendTo( '#'+layoutId );
	}

	function addQueueRow(queueRow)
	{
		addProcessRow(queueRow);
	}
 
 </script>
