<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script type="text/javascript">

  	var windowManager = new WindowManager();
	
	function WindowManager()
	{
		this.currentView = 'process-panel-view';
		this.viewHistory = [];
		this.mobileMode = false;
		this.tabletMode = false;
		this.allViews = {};
		this.oldUrl = document.URL;
		
		this.addView = function(viewId)
		{
			this.allViews[viewId] = new WindowView(viewId);
		}
		
		this.previousView = function()
		{
			var lastView = this.viewHistory.pop();
			if(lastView)
			{
				<!-- same view, do not show -->
				if(lastView == this.currentView)
				{
					this.previousView();
					return;
				}
				this.showView(this.allViews[lastView], false);
				if(this.allViews[lastView].viewId == 'process-panel-view'  || this.allViews[lastView].viewId == "process-queue-view")
				{
					queueViewManager.reloadCurrentQueue();
				}
			}
		}
		
		this.changeUrl = function(newUrl)
		{
			if(window.history && typeof(window.history.pushState) === 'function')
			{
				var currentState = window.history.location.search;
					
				if(newUrl == '')
				{
					if(!currentState || currentState === '')
						return;
					
					var currentUrl = location.href.replace(/&?taskId=([^&]$|[^&]*)/i, "");
					window.history.pushState('', '', currentUrl);
				}
				else
				{
					if(currentState && currentState === newUrl)
						return;
					
					window.history.pushState('', '', newUrl);
				}
			}
		}
		
		this.showLoadingScreen = function()
		{
			this.showView(this.allViews['loading-screen'], true);
		}
		
		this.showQueueList = function()
		{
			this.showView(this.allViews['outer-queues'], true);
		}

        this.showSavingScreen = function() {
            $('#saving-screen').show();
        }

		this.hideSavingScreen = function() {
            $('#saving-screen').hide();
        }
		
		this.showConfiguration = function()
		{
			this.showView(this.allViews['configuration'], true);
		}
		
		this.showSearchProcessPanel = function()
		{
			this.showView(this.allViews['search-view'], true);
		}
		
		this.showNewProcessPanel = function()
		{
			this.showView(this.allViews['new-process-view'], true);
		};
		
		this.showProcessList = function(data)
		{
		    widgets = [];
			this.changeUrl('');
			this.showView(this.allViews['process-queue-view'], true);
			$("#process-queue-view").append(data);
		}
		
		this.showProcessData = function(data)
		{
		    widgets = [];
			this.showView(this.allViews['process-data-view'], true);
			$('#actions-list').fadeIn(600);
			$("#process-data-view").append(data);
		}

        this.showProcessDataImmediate = function()
        {
            this.showView(this.allViews['process-data-view'], true);
            $('#actions-list').show();
        }
		
		this.hasPreviousView = function()
		{
			return this.viewHistory.length > 0;
		}
		
		this.addError = function(errorMessage)
		{
			if($("#error-screen").is(":visible") == false)
			{
				$("#error-screen").fadeIn(500);
				$("#loading-screen").hide();
			}
			
			$('#error-screen').append('<div class="alert alert-error"><button type="button" class="close" data-dismiss="alert">&times;</button>'+errorMessage+'</div>')
		}
		
		this.clearErrors = function()
		{
			$('#error-screen').empty();
		}		
		
		this.isQueueShown = function()
		{
			return this.currentView == 'process-queue-view';
		}
		
		this.isProcessShown = function()
		{
			return this.currentView == 'process-data-view';
		}
			
		
		this.showView = function(windowView, addToHistory)
		{
			$(document.getElementById(this.currentView)).stop(true, true);
			
			if(this.tabletMode == true && $("#mobile-collapse").hasClass('in') == true)
			{
				$("#mobile-collapse").collapse('hide');
			}
			windowManager.clearProcessView();
			windowManager.clearQueueView();
			
			$.each(this.allViews, function(index, view ) 
			{ 
				if(this != windowView.viewId)
				{
					$(document.getElementById(view.viewId)).hide();
				}
			});
			
			if("loading-screen" != this.currentView && this.currentView != windowView.viewId)
			{
				this.viewHistory.push(this.currentView);
			}
			
			this.currentView = windowView.viewId;
			$(document.getElementById(windowView.viewId)).fadeIn(500);
		}
		
		this.clearQueueView = function()
		{
			$('#process-queue-view').empty();
		}

		this.hideCurrentView = function()
		{
			$('#'+this.currentView).hide();
		}
		
		
		this.clearProcessView = function()
		{
			//this.changeUrl('');
			$('#actions-list').empty();
			$('#process-data-view').empty();
			
			widgets = [];
			
			<!-- required to close vaadin application -->
			$('.vaadin-widget-view').each(function( ) 
			{ 
				var widgetToClose = $(this);
				var widgetId = $(this).attr('widgetId');
				var taskId = $(this).attr('taskId');
				
				var windowName = taskId+"_"+widgetId;
				
				var source = "widget/"+windowName+"_close/";
				var url = '<spring:url value="/'+source+'"/>';
				
				
				$.ajax(url)
				.done(function() 
				{
					widgetToClose.remove();
				});
				
			});
			
			vaadinWidgetsCount = 0;
			vaadinWidgetsLoadedCount = 0;
		}
	}
	
		
	function XOR(a,b) {
	  return ( a || b ) && !( a && b );
	} 
	
	function WindowView(viewId)
	{
		this.viewId = viewId;
	}
</script>