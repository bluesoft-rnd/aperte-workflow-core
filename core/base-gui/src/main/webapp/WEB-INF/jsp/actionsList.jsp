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
        <button id="button-close-comment-modal" type="button" class="close" data-dismiss="modal" onClick="cancelCommentModal()" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel"><spring:message code="processes.action.button.comment.title" /></h4>
      </div>
      <div class="modal-body">
		<div class="modal-errors"></div>
			<spring:message code="processes.action.button.comment.body" />
			
			<textarea id="action-comment-textarea" class="modal-comment-textarea" onkeyup="checkActionCommentValue(event)" ></textarea>
      </div>
      <div class="modal-footer">
        <button id="button-close-comment-modal-2" type="button" class="btn btn-default"  data-dismiss="modal"><spring:message code="processes.action.button.comment.close" /></button>
        <button id="action-comment-button" type="button" class="btn btn-primary" disabled="true"><spring:message code="processes.action.button.comment.perform" /></button>
      </div>
    </div><!-- /.modal-content -->
  </div><!-- /.modal-dialog -->
</div>

<div class="modal fade aperte-modal" id="changeOwnerModal" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" id="button-close-changeowner-modal" class="close" data-dismiss="modal" onClick="cancelChangerOwnerModal()" aria-hidden="true">&times;</button>
        <h4 class="modal-title" id="myModalLabel"><spring:message code="processes.action.button.change.owner.title" /></h4>
      </div>
      <div class="modal-body">
		<div class="modal-owner-errors"></div>
			
			<div class="form-group input-group-sm ">
                <label class="col-sm-4 control-label required" name="tooltip" title='<spring:message code="processes.action.button.change.owner.label.tootip" />' for="change-owner-select"><spring:message code="processes.action.button.change.owner.label" /></label>
                <div id="change-owner-select" class="col-sm-8" data-placeholder="<spring:message code='processes.action.button.change.owner.select.placeholder' />" ></div>
            </div>
      </div>
      <div class="modal-footer">
        <button type="button" id="button-close-changeowner-modal-2" class="btn btn-default"  data-dismiss="modal"><spring:message code="processes.action.button.comment.close" /></button>
        <button id="action-changeowner-button"  type="button" class="btn btn-primary" disabled="true"><spring:message code="processes.action.button.comment.perform" /></button>
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
				<button type="button" class="btn btn-warning" data-dismiss="modal" style="margin: 20px;"><spring:message code="processes.alerts.modal.close" /></button>
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
	var tempChangeOwner;
	var tempChangeOwnerAttrKey;
	
	var alertsShown = false;
	var alertsInit = false;
	
	$(document).ready(function () {
		$("#button-close-changeowner-modal").on("click", function () {
			cancelChangerOwnerModal();
		});
		$("#button-close-changeowner-modal-2").on("click", function () {
			cancelChangerOwnerModal();
		});
		$("#action-changeowner-button").on("click", function () {
			performChangeOwnerModal();
		});
		
		$("#button-close-comment-modal").on("click", function () {
			cancelCommentModal();
		});
		$("#button-close-comment-modal-2").on("click", function () {
			cancelCommentModal();
		});
		$("#action-comment-button").on("click", function () {
			performCommentModal();
		});
	});
	
	function cancelCommentModal()
	{
		$('#commentModal').modal('hide');

	}
	
	function cancelChangerOwnerModal()
	{
		$('#changeOwnerModal').modal('hide');
	}
	
	function performCommentModal()
	{
		var comment = $('#action-comment-textarea').val();
		$('#commentModal').modal('hide');
		if(!comment)
		{
			$('#modal-errors').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button><spring:message code="processes.action.button.comment.empty" /></div>');
			return;
		}
		
		performActionWithoutComment(tempButton, tempActionName, tempSkipSaving, tempTaskId, tempCommentNeeded, comment, tempChangeOwner, '', '');
	}
	
	function performChangeOwnerModal()
	{
		var newOwnerLogin = $('#change-owner-select').val();
		$('#changeOwnerModal').modal('hide');
		if(!newOwnerLogin)
		{
			$('#modal-owner-errors').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button><spring:message code="processes.action.button.change.owner.select.empty" /></div>');
			return;
		}
		
		performActionWithoutComment(tempButton, tempActionName, tempSkipSaving, tempTaskId, false, '',  true, tempChangeOwnerAttrKey, newOwnerLogin);
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
		windowManager.showSavingScreen();
		
		var errors = [];
		<!-- Validate html widgets -->
		$.each(widgets, function() 
		{
			var errorMessages = this.validate();
			if(!errorMessages)
			{

				
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
			windowManager.hideSavingScreen();
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
		var newBpmTask = $.post('<portlet:resourceURL id="saveAction"/>',
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
			if (data.data) {
			    clearAlerts();
                windowManager.showProcessDataImmediate();
                $('#process-data-view').empty();
                $("#process-data-view").append(data.data);
                checkIfViewIsLoaded();
			}
			windowManager.hideSavingScreen();
		})
		.always(function() 
		{ 
			enableButtons();
			windowManager.hideSavingScreen();
		})
		.fail(function(data) 
		{
		    windowManager.hideSavingScreen();
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
				$('#alertModal').appendTo("body").modal({
				  keyboard: false
				});
				$('#alertModal').on('hidden.bs.modal', function (e) {
					clearAlerts();
					alertsShown = false;
				});
			
			}
			else
			{
				$('#alertModal').appendTo("body").modal('show');
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
	function performAction(button, actionName, skipSaving, commentNeeded, changeOwner, changeOwnerAttributeKey, taskId)
	{
		if(commentNeeded == true)
		{

			tempButton = button;
			tempActionName = actionName;
			tempSkipSaving = skipSaving;
			tempTaskId = taskId;
			tempCommentNeeded = commentNeeded;
			
			
			$('#action-comment-textarea').val('');
			$('#action-comment-button').prop("disabled", true);
			$('#commentModal').appendTo("body").modal({
			  keyboard: false
			});
			$('#commentModal').on('hidden.bs.modal', function (e) {
			  	$('#modal-errors').empty();
				$('#action-comment-textarea').val('');
				enableButtons();
			});
			
		}
		else if(changeOwner == true)
		{
			tempButton = button;
			tempActionName = actionName;
			tempSkipSaving = skipSaving;
			tempTaskId = taskId;
			tempCommentNeeded = commentNeeded;
			tempChangeOwner = changeOwner;
			tempChangeOwnerAttrKey = changeOwnerAttributeKey;
			
			
			$('#change-owner-select').val('');
			$('#changeOwnerModal').appendTo("body").modal({
			  keyboard: false
			});
			$('#changeOwnerModal').on('hidden.bs.modal', function (e) {
			  	$('#modal-errors').empty();
				$('#change-owner-select').val('');
				enableButtons();
			});
			
			$("#change-owner-select").select2({
				minimumInputLength: 3,
				ajax: {
					url: dispatcherPortlet,
					dataType: 'json',
					quietMillis: 200,
					data: function (term, page) {
						return {
							q: term, // search term
							page_limit: 10,
							controller: "usercontroller",
							page: page,
							action: "getAllUsers"
						};
					},
					results: function (data, page)
					{
						var results = [];
						  $.each(data.data, function(index, item){
							results.push({
							  id: item.login,
							  text: item.firstName +" "+ item.lastName+" ["+item.login+"]"
							});
						  });
						  return {
							  results: results
						  };
					}
				}
			});
			$("#change-owner-select").on("change", function(e) 
			{
				var selectedLogin = $('#change-owner-select').val();
				$('#action-changeowner-button').attr("disabled", (!selectedLogin || selectedLogin == ''));
			});
		}
		else
		{
			performActionWithoutComment(button, actionName, skipSaving, taskId, false, '', false, '', '');
		}
		
	}
	
	
	function performActionWithoutComment(button, actionName, skipSaving, taskId, commentNeeded, comment, changeOwner, changeOwnerAttributeKey, changeOwnerAttributeValue)
	{
		var widgetData = [];

		if(skipSaving != true)
		{
			clearAlerts();
			
			var errors = [];
			<!-- Validate html widgets -->
			$.each(widgets, function() 
			{
				var errorMessages = this.validate(actionName);
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
			
			$.each(widgets, function() 
			{
				var widgetDataBean = new WidgetDataBean(this.widgetId, this.name, this.getData());
				widgetData.push(widgetDataBean);
			});
		}

		var performActionArgs =
		{
			button: button,
			actionName: actionName,
			skipSaving: skipSaving,
			taskId: taskId,
			commentNeeded: commentNeeded,
			comment: comment,
			changeOwner: changeOwner,
			changeOwnerAttributeKey: changeOwnerAttributeKey,
			changeOwnerAttributeValue: changeOwnerAttributeValue,
			widgetData: widgetData
		};

        for (var i = 0; i < widgets.length; ++i)
        {
        	var widget = widgets[i];

        	if (widget.beforePerformAction)
        	{
        		var handled = widget.beforePerformAction(performActionRequest, performActionArgs);

        		if (handled)
        		{
        			return;
        		}
        	}
        }

        // no widget intercepted the action -> default handling

		performActionRequest(performActionArgs);
	}

	function performActionRequest(args)
	{
		var JsonWidgetData = "[{}]";

		if (args.widgetData.length > 0)
		{
			JsonWidgetData = JSON.stringify(args.widgetData, null, 2);
		}

		var newBpmTask = $.post('<portlet:resourceURL id="performAction"/>',
		{
			"taskId": args.taskId,
			"actionName": args.actionName,
			"skipSaving": args.skipSaving,
			"commentNeeded": args.commentNeeded,
			"comment": args.comment,
			"changeOwner": args.changeOwner,
			"changeOwnerAttributeKey": args.changeOwnerAttributeKey,
			"changeOwnerAttributeValue": args.changeOwnerAttributeValue,
			"widgetData": JsonWidgetData
		})
		.done(function(data)
		{
			<!-- Errors handling -->
			windowManager.clearErrors();

			var errors = [];
			$.each(data.errors, function() {
				errors.push(this);
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

    function removeAllButOne(selector)
    {
    	while ($(selector).size() > 1)
    	{
    		$(selector).last().remove();
    	}
    }
//]]>
</script>