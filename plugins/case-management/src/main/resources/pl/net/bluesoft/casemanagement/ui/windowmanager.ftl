<#import "/spring.ftl" as spring />

<#assign portlet=JspTaglibs["http://java.sun.com/portlet_2_0"] />

<script type="text/javascript">

	var iconsBaseUrl = '/aperteworkflow<@spring.url '/images/documents_icons'/>';

	var fileIcons = {
		'default' : 'default.png',
		'txt' : 'txt.png',
		'pdf' : 'pdf.png',
		'bmp' : 'bmp.png',
		'gif' : 'gif.png',
		'jpg' : 'jpg.png',
		'jpeg' : 'jpg.png',
		'ods' : 'ods.png',
		'odt' : 'odt.png',
		'zip' : 'zip.png',
		'rar' : 'rar.png',
		'7z' : '7zip.png',
		'msg' : 'msg.png',
		'png' : 'png.png',
		'doc' : 'doc.png',
		'docx' : 'docx.png',
		'ppt' : 'ppt.png',
		'pptx' : 'ppt.png',
		'xls' : 'xls.png',
		'xlsx' : 'xlsx.png',
		'' : 'default.png'
	};
	function getFileIcon(filename){
		if (typeof fileIcons == 'undefined') {console.log('WARNING! fileIcons undefined'); return;}
		if (typeof fileIcons != 'object') {console.log('WARNING! fileIcons not an object'); return;}
		if (typeof fileIcons['default'] == 'undefined') {console.log('WARNING! fileIcons["default"] undefined');}
		if (typeof filename == 'undefined') return '';
		if (typeof filename != 'string') return '';
		var extDotIndex = filename.lastIndexOf(".");
		if (extDotIndex <= -1){
			return fileIcons['default'];
		}
		var ext = filename.substring(extDotIndex+1).toLowerCase();;
		var iconFile = fileIcons[ext];
		if (typeof iconFile == 'undefined'){
			return fileIcons['default'];
		}
		return iconFile;
	}

	function getFileIconUrl(filename){
		if (typeof getFileIcon != 'function'){console.log('WARNING! getFileIcon undefined'); return;}
		var iconFileName = getFileIcon(filename);
		if (!iconFileName){
			return '';
		} else {
			return iconsBaseUrl + '/' + iconFileName;
		}
	}

	function getFileIconHtml(filename){
		if (typeof getFileIcon != 'function'){console.log('WARNING! getFileIconUrl undefined'); return;}
		var iconFileUrl = getFileIconUrl(filename);
		if (!iconFileUrl){
			return '';
		} else {
			return '<img src="'+iconFileUrl+'" style="width: 16px; height: 16px;" />';
		}
	}

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
				if(this.allViews[lastView].viewId == 'process-panel-view')
				{
					queueViewManager.reloadCurrentQueue();
				}
			}
		}

		this.changeUrl = function(newUrl)
		{
			if(window.history && typeof(window.history.pushState) === 'function')
			{
				if(newUrl == '')
				{
					var currentUrl = location.href.replace(/&?taskId=([^&]$|[^&]*)/i, "");
					window.history.pushState('', '', currentUrl);
				}
				else
				{
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

		this.showProcessList = function()
		{
			this.changeUrl('');
			this.showView(this.allViews['process-panel-view'], true);
		}

		this.showProcessData = function()
		{
			this.showView(this.allViews['process-data-view'], true);
			$('#actions-list').fadeIn(600);
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



		this.showView = function(windowView, addToHistory)
		{
			$(document.getElementById(this.currentView)).stop(true, true);

			if(this.tabletMode == true && $("#mobile-collapse").hasClass('in') == true)
			{
				$("#mobile-collapse").collapse('hide');
			}
			windowManager.clearProcessView();

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


		this.clearProcessView = function()
		{
			//this.changeUrl('');
			$('#actions-list').empty();

			widgets = [];

			<!-- required to close vaadin application -->
			$('.vaadin-widget-view').each(function( )
			{
				var widgetToClose = $(this);
				var widgetId = $(this).attr('widgetId');
				var taskId = $(this).attr('taskId');

				var windowName = taskId+"_"+widgetId;

				var source = "widget/"+windowName+"_close/";
				var url = '<@spring.url "/'+source+'"/>';


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