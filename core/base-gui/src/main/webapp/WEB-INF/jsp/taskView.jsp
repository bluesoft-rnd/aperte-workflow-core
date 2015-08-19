﻿<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<div class="process-panel" id="process-panel-view" hidden="true">
	<div class="process-queue-name apw_highlight" id="process-queue-name-id">
		 
	</div>

</div>

<script type="text/javascript">
//<![CDATA[
	
	$(document).ready(function()
	{
		windowManager.addView("process-panel-view");
	});
	
	function loadCookies(queueName){
		var selectedProcess = $.cookie("pl.net.aperteworkflow."+queueName+"Config");
		if (!selectedProcess){
			selectedProcess = selectAllCheckboxes(queueName);
			saveProcessCookie(selectedProcess,queueName);
		}
	return selectedProcess
	}
	
	function selectAllCheckboxes(viewName){
		var list = $('input:checkbox[name='+viewName+']').map(function() {
			return '"'+$(this).val() +'":'+ true;
		}).get().join();
		list = "{"+list+"}";
		return list;
	}
	
	function saveProcessCookie(selectedProcess,queueName){
		$.cookie("pl.net.aperteworkflow."+queueName+"Config",selectedProcess,{ expires: 365 });
	}
	
	function parseCookie(loadedCookie){
		return jQuery.parseJSON(loadedCookie);
	}

//]]>
</script>