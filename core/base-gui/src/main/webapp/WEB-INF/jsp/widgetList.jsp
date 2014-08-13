<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<div id="process-data-view" class="process-data-view" hidden="true">
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
	
	$(document).ready(function()
	{
		windowManager.addView("process-data-view");
	});
	
	<!-- Widget class  -->
	function Widget (name, widgetId, taskId)
	{
		this.name = name;
		this.widgetId = widgetId;
		this.taskId = taskId;
		this.formId = 'test';
		this.validate = function() {};
		this.validateDataCorrectness = function() {};
		this.getData = function() { return null; };
		this.isEnabled = true;
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
			$(iframe).attr('widgetLoaded', false);
		
		}
		else
		{
			$(iframe).attr('widgetLoaded', true);
		}			
		vaadinWidgetsLoadedCount += 1;  
		checkIfViewIsLoaded();
	}
	
	function checkIfViewIsLoaded()
	{
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

	  if($.browser)
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
	
	function initGoogleMap(divId, address)
	{
		geocoder = new google.maps.Geocoder();

		geocoder.geocode( { 'address': address}, function(results, status) {
			if (status == google.maps.GeocoderStatus.OK) {

				var myOptions = {
					zoom: 15,
					center: results[0].geometry.location,
					mapTypeId: google.maps.MapTypeId.ROADMAP
				};
				var map = new google.maps.Map(document.getElementById(divId),myOptions);

				var marker = new google.maps.Marker({
					map: map, 
					position: results[0].geometry.location
				});
			} else {
				alert("Google was not able to locate the address for this reason: " + status);
			}
		});
	}
	
	function initRouteGoogleMap(divId, address, destinationAddress)
	{
		geocoder = new google.maps.Geocoder();
		
		
		geocoder.geocode( { 'address': address}, function(results, status) {
			if (status == google.maps.GeocoderStatus.OK) 
			{

				var myOptions = {
					zoom: 15,
					center: results[0].geometry.location,
					mapTypeId: google.maps.MapTypeId.ROADMAP
				};
				var map = new google.maps.Map(document.getElementById(divId),myOptions);

				var marker = new google.maps.Marker({
					map: map, 
					position: results[0].geometry.location
				});
								var directionsService = new google.maps.DirectionsService();
				var directionsDisplay = new google.maps.DirectionsRenderer();
				directionsDisplay.setMap(map);
				
				var request = {
					origin:address,
					destination:destinationAddress,
					travelMode: google.maps.TravelMode.DRIVING
				  };
				directionsService.route(request, function(result, status) 
				{
					if (status == google.maps.DirectionsStatus.OK) {
					  directionsDisplay.setDirections(result);
					  $('#'+divId).find('.loader').css('display', 'none');
					}
				  });
			} else {
				alert("Google was not able to locate the address for this reason: " + status);
			}
		});
	}

//]]>
</script>