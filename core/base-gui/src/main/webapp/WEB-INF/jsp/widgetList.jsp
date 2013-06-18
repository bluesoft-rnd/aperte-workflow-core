<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="process-data-view" class="process-data-view">
	<div id="alerts-list" class="process-alerts">
	</div>
	<div id="vaadin-widgets" class="vaadin-widgets-view">

	</div>	
	<div id="actions-list" class="actions-view">
	</div>
</div>

<script type="text/javascript">
//<![CDATA[	
	<!-- Create widgets -->
	var widgets = [];
	
	var vaadinWidgetsCount = 0;
	var vaadinWidgetsLoadedCount = 0;
	
	<!-- Widget class  -->
	function Widget (name, widgetId, taskId)
	{
		this.name = name;
		this.widgetId = widgetId;
		this.taskId = taskId;
		this.formId = 'test';
		this.validate = function() {};
		this.getData = function() { return null; };
	}
	
	function WidgetDataBean(widgetId, widgetName, data)
	{
		this.widgetId = widgetId;
		this.data = data;
		this.widgetName = widgetName;
	}
	
	function onLoadIFrame(iframe)
	{
		var isVis = $(iframe).is(":visible");
		if(isVis == false)
		{
			console.log($(iframe).attr('id')+" mark unloaded!" ); 
			$(iframe).attr('widgetLoaded', false);
		
		}
		else
		{
			$(iframe).attr('widgetLoaded', true);
		}			
		vaadinWidgetsLoadedCount += 1;  
		console.log( "vaadinWidgetsLoadedCount: "+vaadinWidgetsLoadedCount + ", vaadinWidgetsCount: "+vaadinWidgetsCount ); 
		if(vaadinWidgetsCount == vaadinWidgetsLoadedCount)
		{
			enableButtons();
		}
	}
	
	function onTabChange(e)
	{
	  showVaadinWidget(e.target);
			
	  e.target // activated tab
	  e.relatedTarget // previous tab

	  if($.browser.mozilla)
	  {
		  $("[id^='iframe-vaadin-']").each(function( ) 
		  {
			var isVis = $(this).is(":visible");
			var isWidgetLoaded = $(this).attr('widgetLoaded');
					
			if((isVis == true) && (isWidgetLoaded == "false"))
			{
				$(this).attr('src', $(this).attr('src'));
			}
		  });
		}
	}
	
	<!-- Need this to resolve vaadin iframe size problem with tabs -->
	function showVaadinWidget(element)
	{
		$('.vaadin-widget-view').each(function( ) 
		{
			$(this).width("100%") ;
		});
	}

//]]>
</script>