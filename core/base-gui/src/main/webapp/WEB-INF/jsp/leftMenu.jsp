<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="left-menu">
	<div class="start-process-button" id="process-start-button">
		<spring:message code="processes.start.new.process" />
	</div>
	<div class="search-process-button" id="show-search-view-button">
		<spring:message code="processes.search.process" />
	</div>
	<div class="queues-list" id="queue-view-block">

	</div>
</div>
 <script type="text/javascript">
 
	var userLogin = '${aperteUser.login}';
	$(document).ready(function()
	{
		$('#new-process-view').hide();
		$('#process-data-view').hide();
		reloadQueues();
	});
 
	$("#process-start-button").click(
	  function () 
	  {
		windowManager.showNewProcessPanel();
	  }
	);
	
	$("#show-search-view-button").click(
	  function () 
	  {
		windowManager.showSearchProcessPanel();
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
				var currentUserLogin = this.userLogin;
				var userQueueHeaderId = 'accordion-header-'+currentUserLogin;
				var userQueuesCount = this.activeTasks;
				
				var queueName = '<spring:message code="queues.user.queueName" />';
				console.log( "currentUserLogin: "+currentUserLogin+", userLogin: "+userLogin+", userQueuesCount: "+userQueuesCount); 
				if(currentUserLogin != userLogin)
				{
					queueName = currentUserLogin;
				}
				
				queueName += " ["+userQueuesCount+"]";
				
				var accordionID = 'accordion-list-'+currentUserLogin;
				$( "<a>", { id: userQueueHeaderId, text: queueName, "data-toggle":"collapse", "data-parent":'#queue-view-block', href:"#"+accordionID, "class": "queue-user-accordion"} )
				.appendTo( '#queue-view-block' );
				
				var contentClass = "accordion-body collapse in";

				
				$( "<div>", { id : accordionID, "class": contentClass} )
				.appendTo( '#queue-view-block' );
				
				$.each( this.processesList, function( ) 
				{
					addProcessRow(this, accordionID, currentUserLogin);
				});
				
				$.each( this.queuesList, function( ) 
				{
					addQueueRow(this, accordionID, currentUserLogin);
				});
			});
		});
	}
	
	function addProcessRow(processRow, accordionID, userLogin)
	{
		var layoutId = 'queue-view-' + processRow.queueId+'-'+userLogin;
		var innerDivId = processRow.queueId+'-'+userLogin;

		$( "<div>", { id : layoutId, "class": "queue-list-row-process"} )
		.appendTo( '#'+accordionID );
		
		$( "<div>", { id : innerDivId, "class": "queue-list-name"} )
		.appendTo( '#'+layoutId );
		
		$( "<a>", { id : 'link-'+processRow.queueId, "class": "queue-list-link", text: processRow.queueDesc, "onclick":"reloadQueue('"+processRow.queueName+"', 'process', '"+userLogin+"') "} )
		.appendTo( '#'+innerDivId );
		
		$( "<div>", { "class": "queue-list-size", text: processRow.queueSize} )
		.appendTo( '#'+layoutId );
		
		$( "<br>", { style: "clear: left;"} )
		.appendTo( '#'+layoutId );
	}

	function addQueueRow(queueRow, accordionID, userLogin)
	{
		var layoutId = 'queue-view-' + queueRow.queueId+'-'+userLogin;
		var innerDivId = queueRow.queueId+'-'+userLogin;

		$( "<div>", { id : layoutId, "class": "queue-list-row-queue"} )
		.appendTo( '#'+accordionID );
		
		$( "<div>", { id : innerDivId, "class": "queue-list-name"} )
		.appendTo( '#'+layoutId );
		
		$( "<a>", { id : 'link-'+queueRow.queueId, "class": "queue-list-link", text: queueRow.queueDesc, "onclick":"reloadQueue('"+queueRow.queueName+"', 'queue', '"+userLogin+"') "} )
		.appendTo( '#'+innerDivId );
		
		$( "<div>", { "class": "queue-list-size", text: queueRow.queueSize} )
		.appendTo( '#'+layoutId );
		
		$( "<br>", { style: "clear: left;"} )
		.appendTo( '#'+layoutId );
	}
 
 </script>
