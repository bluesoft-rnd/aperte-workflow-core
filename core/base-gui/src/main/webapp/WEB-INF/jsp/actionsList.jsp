<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<div class="modal fade aperte-modal" id="commentModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" onClick="cancelCommentModal()" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel"><spring:message code="processes.action.button.comment.title" /></h4>
      </div>
      <div class="modal-body">
		<div class="modal-errors"></div>
			<spring:message code="processes.action.button.comment.body" />
			
			<textarea id="action-comment-textarea" class="modal-comment-textarea" onkeyup="checkActionCommentValue(event)" ></textarea>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" onClick="cancelCommentModal()" data-dismiss="modal"><spring:message code="processes.action.button.comment.close" /></button>
        <button id="action-comment-button" type="button" onClick="performCommentModal()" class="btn btn-primary" disabled="true"><spring:message code="processes.action.button.comment.perform" /></button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div>

<div class="modal fade aperte-modal" id="alertModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">


			<div class="panel panel-warning">
				<div class="panel-heading"><h4><spring:message code="processes.alerts.modal.title" /></h4></div>
				<ul id="alerts-list">
					
				</ul>
				<button type="button" class="btn btn-warning" onClick="cancelShowWarnings()" data-dismiss="modal" style="margin: 20px;"><spring:message code="processes.alerts.modal.close" /></button>
			</div>
			


  </div><!-- /.modal-dialog -->
</div>

<script type="text/javascript">
//<![CDATA[
	
	var tempButton;
	var tempActionName;
	var tempSkipSaving; 
	var tempTaskId;
	var tempCommentNeeded;
	
	var alertsShown = false;
	var alertsInit = false;
	
	function cancelCommentModal()
	{
		$('#commentModal').modal('hide');

	}
	
	function performCommentModal()
	{
		$('#commentModal').modal('hide');
		var comment = $('#action-comment-textarea').val();
		if(!comment)
		{
			$('#modal-errors').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button><spring:message code="processes.action.button.comment.empty" /></div>');
			return;
		}
		
		performActionWithoutComment(tempButton, tempActionName, tempSkipSaving, tempTaskId, tempCommentNeeded, comment);
	}


	function disableButtons()
	{
		$('#actions-list').find('button').prop('disabled', true);
	}
	
	function enableButtons()
	{
		$('#actions-list').find('button').prop('disabled', false);
	}
	
	function checkActionCommentValue(event)
    {
        var comment = $('#action-comment-textarea').val();

         $('#action-comment-button').attr("disabled", comment == '');
    }
	
	function saveAction(taskId)
	{
		clearAlerts();
		
		var errors = [];
		<!-- Validate html widgets -->
		$.each(widgets, function() 
		{
			var errorMessages = this.validate();
			if(!errorMessages)
			{
				console.log('Widget should return empty array in validation function! Widget: '+this.name);
				
			}
			else
			{
				$.each(errorMessages, function() {
					errors.push(this);
					addAlert(this);
				});
			}
	    });
		
		if(errors.length > 0)
		{
			enableButtons();
			return;
		}
		
		var widgetData = [];
		
		$.each(widgets, function() 
		{
			var widgetDataBean = new WidgetDataBean(this.widgetId, this.name, this.getData());
			widgetData.push(widgetDataBean);
	    });
		
		var JsonWidgetData = JSON.stringify(widgetData, null, 2);
		
		var state = 'OK';
		var newBpmTask = $.getJSON('<portlet:resourceURL id="saveAction"/>',
		{
			"taskId": taskId,
			"widgetData": JsonWidgetData
		})
		.done(function(data)
		{
			if(data.errors != null)
			{
				addAlerts(data.errors);
			}
		})
		.always(function() 
		{ 
			enableButtons();
		})
		.fail(function(data) 
		{ 
			addAlerts(data.errors);
		});
		
		return state;
	}
	
	function addAlerts(alertsMessages)
	{
		$('#alerts-list').empty();
		$.each( alertsMessages, function( ) 
		{
			addAlert(this.message);
		});
	}
	
	function addAlert(alertMessage)
	{
		if(alertsShown == false)
		{
			if(alertsInit == false)
			{
				alertsInit = true;
				$('#alertModal').modal({
				  keyboard: false
				});
				$('#alertModal').on('hidden.bs.modal', function (e) {
					clearAlerts();
					alertsShown = false;
				});
			
			}
			else
			{
				$('#alertModal').modal('show');
			}
			alertsShown = true;
		}
		
		$('#alerts-list').append('<li><h5>'+alertMessage+'</h5></li>');
		
	}
	
	function clearAlerts()
	{
		$('#alerts-list').empty();
	}
	
	<!-- Check for comment required field -->
	function performAction(button, actionName, skipSaving, commentNeeded, taskId)
	{
		if(commentNeeded == true)
		{

			tempButton = button;
			tempActionName = actionName;
			tempSkipSaving = skipSaving;
			tempTaskId = taskId;
			tempCommentNeeded = commentNeeded;
			
			$('#action-comment-textarea').val('');
			$('#commentModal').modal({
			  keyboard: false
			});
			$('#commentModal').on('hidden.bs.modal', function (e) {
			  	$('#modal-errors').empty();
				$('#action-comment-textarea').val('');
				enableButtons();
			});
			
		}
		else
		{
			performActionWithoutComment(button, actionName, skipSaving, taskId, false, '');
		}
		
	}
	
	
	function performActionWithoutComment(button, actionName, skipSaving, taskId, commentNeeded, comment)
	{
		var JsonWidgetData = "[{}]";

		if(skipSaving != true)
		{
			clearAlerts();
			
			var errors = [];
			<!-- Validate html widgets -->
			$.each(widgets, function() 
			{
				var errorMessages = this.validate();
				$.each(errorMessages, function() {
					errors.push(this);
					addAlert(this);
				});
			});
			
			if(errors.length > 0)
			{
				enableButtons();
				return;
			}
			
			var widgetData = [];
			
			$.each(widgets, function() 
			{
				var widgetDataBean = new WidgetDataBean(this.widgetId, this.name, this.getData());
				widgetData.push(widgetDataBean);
			});
			
			JsonWidgetData = JSON.stringify(widgetData, null, 2);
		}
		
		var newBpmTask = $.getJSON('<portlet:resourceURL id="performAction"/>',
		{
			"taskId": taskId,
			"actionName": actionName,
			"skipSaving": skipSaving,
			"commentNeeded": commentNeeded,
			"comment": comment,
			"widgetData": JsonWidgetData
		})
		.done(function(data)
		{
			<!-- Errors handling -->
			windowManager.clearErrors();
			
			var errors = [];
			$.each(data.errors, function() {
				errors.push(this);
				console.log( "error: "+this.message); 
				addAlert(this.message);
			});
			
			if(errors.length > 0) { return; }
			
			reloadQueues();
			
			if(!data)
			{
			    closeProcessView();
				queueViewManager.reloadCurrentQueue();
				windowManager.showProcessList();
				
				return;
			}
			else if(data.errors.length > 0)
			{
				addAlerts(data.errors);
				return;
			}
			else if(!data.nextTask)
			{
				closeProcessView();
				queueViewManager.reloadCurrentQueue();
				windowManager.showProcessList();
				
				return;
			}

			var taskId = data.nextTask.taskId;
			if(taskId)
			{
				loadProcessView(taskId);
			}
			else
			{
				closeProcessView();
				queueViewManager.reloadCurrentQueue();
				windowManager.showProcessList();
			}
		})
		.fail(function() { addAlerts(data.errors); })
		.always(function(data) 
		{ 
			if(data != null)
			{
				enableButtons();
			}
		});
	}
	
	function onSaveButton(taskId)
	{
		disableButtons();
		saveAction(taskId);
	}
	
	function onCancelButton()
	{
		reloadQueues();
		disableButtons(); 
		windowManager.previousView();
	}
	
	function closeProcessView()
	{
		$('#vaadin-widgets').empty();
		$('#actions-list').empty();
		
		windowManager.showProcessList();
	}


//]]>
</script>