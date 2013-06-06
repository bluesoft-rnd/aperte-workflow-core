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
	var vaadinWidgetsCount = 0;
	var vaadinWidgetsLoadedCount = 0;
	
	function appendWidget(widget, parentId, taskId)
	{
		console.log( "classname:" + widget.className); 
		switch(widget.className)
		{
			case "TabSheet":
			{
				
				var tabId = 'tab_sheet_'+widget.id;
				var divContentId = 'div_content_'+widget.id;
				
				$( "<ul>", { id : tabId, "class": "nav nav-tabs" } )
				.appendTo( parentId );
				
				$( "<div>", { id : divContentId, "class": "tab-content" } )
				.appendTo( parentId );
				
				$.each( widget.children, function( ) 
				{
					var elementId = "tab" + this.id;

					
					var htmlTab = '<li><a id="tab_link_'+elementId+'" href="#'+elementId+'" data-toggle="tab"><spring:message code="'+this.caption+'" /></a></li>';
					var innerHtmlTab = '<div id="'+elementId+'" class="tab-pane"></div>';
					
					
					$("#"+tabId).append(htmlTab);
					$("#"+divContentId).append(innerHtmlTab);
					
					$("#tab_link_"+elementId).on('shown', function (e) 
					{
						showVaadinWidget(e.target);
						
					  e.target // activated tab
					  e.relatedTarget // previous tab
					});
					
					appendWidget(this, "#"+elementId, taskId);
				});
					$('#'+tabId+' a:last').tab('show');
				break;
			}
			case "VerticalLayout":
			{
				var layoutId = 'vertical_layout' + widget.id;
				
				$( "<div>", { id : layoutId, text : widget.id } )
				.appendTo( parentId );
				
				$.each( widget.children, function( ) 
				{
					appendWidget(this, "#"+layoutId, taskId);
				});
				
				break;
			}
			default: 
			{
			 <!-- You have to lave widgetid in adress or vaadin would have problems with windows management -->
			  vaadinWidgetsCount += 1;
			  var vaadinWidgetUrl = "widget/"+widget.id+"/?widgetId=" + widget.id + "&taskId="+taskId;
			  console.log( "url:" + vaadinWidgetUrl); 
			  
			  $( "<iframe>", { 
					  src : '<spring:url value="/'+vaadinWidgetUrl+'"/>', 
					  autoResize: true, 
					  id: 'iframe-vaadin-'+widget.id,
					  frameborder: 0,
					  "class": "vaadin-widget-view"
					  } )
			    .load(function() 
				{ 			
					vaadinWidgetsLoadedCount += 1;  
					console.log( "vaadinWidgetsLoadedCount: "+vaadinWidgetsLoadedCount + ", vaadinWidgetsCount: "+vaadinWidgetsCount ); 
					if(vaadinWidgetsCount == vaadinWidgetsLoadedCount)
					{
						enableButtons();
					}
				} )
				.appendTo( parentId );
				
				
				break;
			} 
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