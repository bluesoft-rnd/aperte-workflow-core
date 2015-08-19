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
<div id="process-queue-view" class="process-data-view" hidden="true"></div>

<script type="text/javascript">

    function sticky_relocate() {
        var window_top = $(window).scrollTop() + 36;

        var anchors =  $('[class*=fixed-element-anchor-]');
        if(!anchors) { return; }
        anchors.each(function(i, e)
        {
            var clses = e.className.split(/\s+/);
            if(!clses) { return; }
            var id = null
            for(var i = 0;i<clses.length;i++)
            {
                if(clses[i].match(/^fixed-element-anchor-.*$/))
                    {
                        id = clses[i].substr("fixed-element-anchor-".length);
                        break;
                    }
            }

            var anchor = $(e)
            var el = $('.fixed-element-' + id)

            var div_top = anchor.offset().top;
                        if (window_top > div_top) {
                            el.addClass('sticky');
                        } else {
                            el.removeClass('sticky');
                        }
             el.css({width: anchor.width() + "px"});
        })
    }

    $(function () {
        $(window).scroll(sticky_relocate);
        $(window).resize(sticky_relocate);
        sticky_relocate();
    });

	
	var vaadinWidgetsCount = 0;
	var vaadinWidgetsLoadedCount = 0;
	
	$(document).ready(function()
	{
		windowManager.addView("process-data-view");
		windowManager.addView("process-queue-view");
	});

	
	
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

    var delay = (function(){
                var timer = 0;
                return function(callback, ms){
                    clearTimeout (timer);
                    timer = setTimeout(callback, ms);
                };
            })();

    $('#processInputTextField').keyup(function()
    {

        delay(function(){
          $('#process-queue-view table').dataTable().fnFilter( $('#processInputTextField').val() );
        }, 500 );
    });

</script>