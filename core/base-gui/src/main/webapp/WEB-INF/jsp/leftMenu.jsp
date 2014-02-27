<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<div class="navbar left-menu" >
	<nav id="mobile-collapse" class="navbar navbar-default left-menu" role="navigation">

			<ul class="nav left-menu">
                      <li><a id="priv-view-link" class="left-menu-link" href="#" onclick="windowManager.previousView();"><spring:message code="navigation.previous" /></a></li>
            </ul>
			  <a class="btn btn-navbar navbar-toggle left-menu" data-toggle="collapse" data-target="#mobile-collapse-inner">
				<span class="sr-only">Toggle navigation</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			  </a>
			<div id="mobile-collapse-inner" class="collapse navbar-collapse left-menu btn-group-vertical">

				<button  type="button" class="btn btn-success" id="process-start-button" onClick="windowManager.showNewProcessPanel();">
					<i class="icon-briefcase icon-white" ></i><spring:message code="processes.start.new.process" />
				</button >
				<button  type="button" class="btn btn-default" id="show-search-view-button" onClick="windowManager.showSearchProcessPanel();">
					<i class="icon-search icon-white" ></i><spring:message code="processes.search.process" />
				</button >
				<button type="button" class="btn btn-default"  id="show-configuration-view-button" onClick="windowManager.showConfiguration();">
					<i class="icon-wrench icon-white" ></i><spring:message code="processes.show.configuration" />
				</button>
			</div>
			<br>
			<div id="inner-queues">
				<div class="panel-group" id="queue-view-block">

				</div>
			</div>

	</nav>
</div>
<div class="navigation-border" ></div>
<div class="outer-queue-list" id="outer-queues">
</div>


 <script type="text/javascript">

	var userLogin = '${aperteUser.login}';
	var queueInterval = '${queueInterval}';
	var reloadQueuesLoopTimer;

	$(document).ready(function()
	{	
		windowManager.addView("outer-queues");
		reloadQueuesLoopTimer = $.timer(function()
        	{
               reloadQueues();
            });
		reloadQueuesLoopTimer.set({ time : queueInterval, autostart : true });
	});

	function moveQueueList()
	{
		if($(window).width() < 479 && windowManager.mobileMode == false)
		{
			windowManager.mobileMode = true;
			queueViewManager.enableMobileMode();
			


			//toggleColumnButton(2, false);
			//toggleColumnButton(3, false);
		}
		if($(window).width() < 768 && windowManager.tabletMode == false)
		{
			windowManager.tabletMode = true;
			queueViewManager.enableTabletMode();
			$('#queue-view-block').appendTo('#outer-queues');
			


			//toggleColumnButton(4, false);
			//toggleColumnButton(5, false);
		}

		if($(window).width() >= 480 && windowManager.mobileMode == true)
		{
			windowManager.mobileMode = false;
			queueViewManager.disableMobileMode();
			


			//toggleColumnButton(2, true);
			//toggleColumnButton(3, true);
		}
		if($(window).width() >= 768 && windowManager.tabletMode == true)
		{
			windowManager.tabletMode = false;
			queueViewManager.disableTabletMode();
			$('#queue-view-block').appendTo('#inner-queues');
			

			//toggleColumnButton(4, true);
			//toggleColumnButton(5, true);

		}

	}

	$(window).resize(function()
	{
		moveQueueList();

	});

	var oldProcessCount = -1;

	function reloadQueues()
	{
		reloadQueuesLoopTimer.pause();
		try
		{
			var queuesJson = $.post('<portlet:resourceURL id="getUserQueues"/>', function(queues)
			{ 
				$('#queue-view-block').empty();
				
				$.each( queues, function( ) 
				{
					var currentUserLogin = this.userLogin;
					var userQueueHeaderId = 'accordion-header-'+currentUserLogin;
					var userQueuesCount = this.activeTasks;
				
					
					var queueName = '<spring:message code="queues.user.queueName" />';
					if(currentUserLogin != userLogin)
					{
						queueName = currentUserLogin;
					}
					
					queueName += " ["+userQueuesCount+"]";
					
					var accordionID = 'accordion-list-'+currentUserLogin;
					
					$( "<div>", { id : accordionID+"-panel", "class": "panel panel-default"} )
					.appendTo( '#queue-view-block' );
					

					$( "<div>", { id: userQueueHeaderId, "class": "panel-heading panel-heading-aperte-queues"} )
					.appendTo( '#'+accordionID+"-panel" );
					
					$( "<h3>", { id: userQueueHeaderId+"-title", "class": "panel-title panel-title-aperte"} )
					.appendTo( '#'+userQueueHeaderId );
					
					$( "<div>", { id: userQueueHeaderId+"-title-link", text: queueName} )
					.appendTo( '#'+userQueueHeaderId+"-title" );
					
					var contentClass = "panel-body list-group";

					
					$( "<div>", { id : accordionID, "class": contentClass} )
					.appendTo( '#'+accordionID+"-panel" );
					
					
					$.each( this.processesList, function( ) 
					{
						addProcessRow(this, accordionID, currentUserLogin);
						
						<!-- Test current queue for reload only if changed queue is shown and user is viewing process list -->
						if(queueViewManager.currentQueue == this.queueName 
							&& windowManager.currentView == 'process-panel-view'
							&& queueViewManager.currentOwnerLogin == currentUserLogin)
						{

							if(oldProcessCount != this.queueSize)
							{
								queueViewManager.reloadCurrentQueue();
								oldProcessCount = this.queueSize;
							}
						}
					});
					
					$.each( this.queuesList, function( ) 
					{
						addQueueRow(this, accordionID, currentUserLogin);
					});
					

				

				});
			});
		}
		catch(err)
		{

		}
		reloadQueuesLoopTimer.play(true);
	}
	
	function addProcessRow(processRow, accordionID, userLogin)
	{
		var layoutId = 'queue-view-' + processRow.queueId+'-'+userLogin;
		var innerDivId = processRow.queueId+'-'+userLogin;

		$( "<li>", { id : layoutId, "class": "list-group-item list-group-item-left-menu", "data-queue-name": processRow.queueName, "data-user-login" : userLogin, "data-queue-type" : "process", "data-queue-desc" : processRow.queueDesc} )
		.appendTo( '#'+accordionID );
		
		$(document).ready(function () {
			$("#"+layoutId).on("click", function () {
				showQueue( 
						$(this).attr('data-queue-name'),
						$(this).attr('data-queue-type'),
						$(this).attr('data-user-login'),
						$(this).attr('data-queue-desc'));
			});
		});
		
		
		
		$( "<span>", { "class": "badge badge-queue-link", text: processRow.queueSize} )
		.appendTo( '#'+layoutId  );
		$( "<div>", { id : 'link-'+processRow.queueId+'-'+accordionID, "class": "queue-list-link", text: processRow.queueDesc } )
		.appendTo( '#'+layoutId );
		

	}

	function addQueueRow(queueRow, accordionID, userLogin)
	{
		var layoutId = 'queue-view-' + queueRow.queueId+'-'+userLogin;
		var innerDivId = queueRow.queueId+'-'+userLogin;

		$( "<li>", { id : layoutId, "class": "list-group-item list-group-item-left-menu", "data-queue-name": queueRow.queueName, "data-user-login" : userLogin, "data-queue-type" : "queue", "data-queue-desc" : queueRow.queueDesc} )
		.appendTo( '#'+accordionID );
		
		$(document).ready(function () {
			$("#"+layoutId).on("click", function () {
				showQueue( 
						$(this).attr('data-queue-name'),
						$(this).attr('data-queue-type'),
						$(this).attr('data-user-login'),
						$(this).attr('data-queue-desc'));
			});
		});
		
		$( "<div>", { "class": "badge badge-queue-link", text: queueRow.queueSize} )
		.appendTo( '#'+layoutId );
		
		
		$( "<div>", { id : 'link-'+queueRow.queueId, "class": "queue-list-link", text: queueRow.queueDesc } )
		.appendTo( '#'+layoutId );
		

	}
	
	function maouseOverQueue(id){
		$('#'+id).attr('class','alert alert-danger queue-list-row-process');
	}
	
	function maouseOutQueue(id){
		$('#'+id).attr('class','alert alert-info queue-list-row-process');
	}
	
	function showQueue(newQueueName, queueType, ownerLogin, queueDesc)
	{
		reloadQueuesLoopTimer.stop();
		reloadQueuesLoopTimer.play(true);
		reloadQueues();
		queueViewManager.loadQueue(newQueueName, queueType, ownerLogin, queueDesc);
	}
 
 </script>
