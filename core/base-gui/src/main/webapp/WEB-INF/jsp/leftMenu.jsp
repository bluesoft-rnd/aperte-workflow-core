<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="navbar left-menu">
	  <div class="navbar-inner left-menu">
			<div class="container">
			<ul class="nav left-menu">
                      <li><a id="priv-view-link" href="#" onclick="windowManager.previousView();"><spring:message code="navigation.previous" /></a></li>
                    </ul>
			  <a class="btn btn-navbar" data-toggle="collapse" data-target=".navbar-responsive-collapse">
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			  </a>
			  <div id="mobile-collapse" class="nav-collapse collapse navbar-responsive-collapse">
				<div class="start-process-button" id="process-start-button">
					<spring:message code="processes.start.new.process" />
				</div>
				<div class="search-process-button" id="show-search-view-button">
					<spring:message code="processes.search.process" />
				</div>
				<div class="show-queues-button" id="show-queues-view-button">
					<spring:message code="processes.show.queues" />
				</div>
				<div class="inner-queue-list" id="inner-queues">
					<div class="queues-list" id="queue-view-block">

					</div>
				</div>

			  </div><!-- /.nav-collapse -->
			</div>
	  </div><!-- /navbar-inner -->
</div>
<div class="navigation-border" ></div>
<div class="outer-queue-list" id="outer-queues">
</div>


 <script type="text/javascript">
 
	var mobileMode = false;
	var userLogin = '${aperteUser.login}';
	$(document).ready(function()
	{
		$('#new-process-view').hide();
		$('#process-data-view').hide();
		$('#outer-queues').hide();
		
		moveQueueList();
		reloadQueues();
	});
	
	
	function moveQueueList()
	{
		console.log( "width: "+$(window).width()+" mobileMode: "+mobileMode);
		if($(window).width() < 980 && mobileMode == false)
		{
			mobileMode = true;
			$('#queue-view-block').appendTo('#outer-queues');
		}
		else if($(window).width() >= 980 && mobileMode == true)
		{
			mobileMode = false;
			$('#queue-view-block').appendTo('#inner-queues');
		}
	}
	
	$(window).resize(function()
	{
		moveQueueList();
		
	});
	
 
 
	$("#process-start-button").click(function () 
	  {
		$("#mobile-collapse").collapse('hide');
		windowManager.showNewProcessPanel();
	  }
	);
	
	$("#show-search-view-button").click(function () 
	  {
		$("#mobile-collapse").collapse('hide');
		windowManager.showSearchProcessPanel();
	  }
	);
	
	$("#show-queues-view-button").click(function () 
	  {
		$("#mobile-collapse").collapse('hide');
		windowManager.showQueueList();
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

		$( "<div>", { id : layoutId, "class": "queue-list-row-process", "onclick":"reloadQueue('"+processRow.queueName+"', 'process', '"+userLogin+"') "} )
		.appendTo( '#'+accordionID );
		
		$( "<div>", { id : innerDivId, "class": "queue-list-name"} )
		.appendTo( '#'+layoutId );
		
		$( "<a>", { id : 'link-'+processRow.queueId, "class": "queue-list-link", text: processRow.queueDesc, } )
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

		$( "<div>", { id : layoutId, "class": "queue-list-row-queue", "onclick":"reloadQueue('"+queueRow.queueName+"', 'queue', '"+userLogin+"') "} )
		.appendTo( '#'+accordionID );
		
		$( "<div>", { id : innerDivId, "class": "queue-list-name"} )
		.appendTo( '#'+layoutId );
		
		$( "<a>", { id : 'link-'+queueRow.queueId, "class": "queue-list-link", text: queueRow.queueDesc, } )
		.appendTo( '#'+innerDivId );
		
		$( "<div>", { "class": "queue-list-size", text: queueRow.queueSize} )
		.appendTo( '#'+layoutId );
		
		$( "<br>", { style: "clear: left;"} )
		.appendTo( '#'+layoutId );
	}
 
 </script>
