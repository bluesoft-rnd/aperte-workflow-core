<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script type="text/javascript">
//<![CDATA[	

	function disableButtons()
	{
		$('#actions-list').find('button').prop('disabled', true);
	}
	
	function enableButtons()
	{
		$('#actions-list').find('button').prop('disabled', false);
	}

	<!-- Create widgets -->
	function appendAction(action, parentId, taskId)
	{
		console.log( "action name:" + action.actionName); 
		
		$( "<button>", { id : 'action-button-' + action.actionName, text : action.caption, type: "button", "class": "btn btn-large aperte-button", "disabled": true } )
		.click(function () 
			{
			  disableButtons();
			  performAction(this, action.actionName, action.skipSaving, taskId);
			})
		.tooltip({title: action.tooltip})
		.appendTo( parentId );

		

	
	}
	
	function appendSaveAction(parentId, taskId)
	{
		$( "<button>", { id : 'action-button-save', text : '<spring:message code="button.save.process.data" />', type: "button", "class": "btn btn-large btn-success aperte-button","disabled": true  } )
		.click(function () 
			{
			  disableButtons();
			  saveAction(this, taskId);
			})
		.tooltip({title: '<spring:message code="button.save.process.desc" />'})
		.appendTo( parentId );
	}
	
	function appendCancelAction(parentId, taskId)
	{
		$( "<button>", { id : 'action-button-save', text : '<spring:message code="button.cancel" />', type: "button", "class": "btn btn-large btn-inverse aperte-button", "disabled": true  } )
		.click(function () 
			{
			  disableButtons();
			  closeProcessView();
			})
		.tooltip({title: '<spring:message code="button.cancel" />'})
		.appendTo( parentId );
	}
	

	
	
	function saveAction(taskId)
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
		
		var JsonWidgetData = JSON.stringify(widgetData, null, 2);
		
		var state = 'OK';
		var newBpmTask = $.getJSON('<spring:url value="/processes/saveAction.json"/>', 
		{
			"taskId": taskId,
			"widgetData": JsonWidgetData
		})
		.done(function(data) 
		{ 
			console.log( "done, error: "+data.errors.length ); 
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
			console.log( "error: "+data.errors ); 
		});
		
		console.log( "state save: "+state ); 
		
		return state;
	}
	
	function addAlerts(alertsMessages)
	{
		console.log( "alerts: "+alertsMessages );
		$('#alerts-list').empty();
		$.each( alertsMessages, function( ) 
		{
			console.log( "alert: "+this.message );
			addAlert(this.message);
		});
	}
	
	function addAlert(alertMessage)
	{
		$('#alerts-list').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button>'+alertMessage+'</div>')
	}
	
	function clearAlerts()
	{
		$('#alerts-list').empty();
	}
	
	function performAction(button, actionName, skipSaving, taskId)
	{
		var JsonWidgetData = "[{}]";
		console.log( "taskId: "+taskId); 
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
		
		var newBpmTask = $.getJSON('<spring:url value="/processes/performAction.json"/>', 
		{
			"taskId": taskId,
			"actionName": actionName,
			"skipSaving": skipSaving,
			"widgetData": JsonWidgetData
		})
		.done(function(data) 
		{ 
			console.log( "DONE: "+data); 
			reloadQueues();
			
			if(data == null)
			{
			    closeProcessView();
				reloadCurrentQueue();
				showProcessList();
				
				return;
			}
			else if(data.errors.length > 0)
			{
				addAlerts(data.errors);
				return;
			}

			var taskId = data.taskId;
			var processStateConfigurationId = data.processStateConfigurationId;
			
			if(taskId != null)
			{
				loadProcessView(processStateConfigurationId, taskId);
			}
			else
			{
				closeProcessView();
				reloadCurrentQueue();
				showProcessList();
			}
		})
		.fail(function() { addAlerts(data.errors); })
		.always(function(data) 
		{ 
			if(data != null)
			{
				console.log( "enable buttons "); 
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
		showProcessList();
	}
	
	function closeProcessView()
	{
		$('#vaadin-widgets').empty();
		$('#actions-list').empty();
		
		showProcessList();
	}


//]]>
</script>