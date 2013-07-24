<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="new-process-view" class="new-process-block" hidden="true">
	<div class="process-queue-name">
		<spring:message code="new.process.view.header" />
	</div>
	<c:forEach var="processStart" items="${processStartList}">
		<div class="process-start-list-row" onclick="startProcess('${processStart.bpmDefinitionKey}') ">
			<div id="${processStart.bpmDefinitionKey}" class="process-start-name" data-toggle="tooltip" title="<spring:message code='${processStart.description}' /> v.<spring:message code='${processStart.bpmDefinitionVersion}' />"><a class="process-start-link"><spring:message code="${processStart.processName}" /></a></div>
			<div><spring:message code="${processStart.comment}" /> </div>
		</div>
	</c:forEach>
</div>

 <script type="text/javascript">
  	$(document).ready(function()
	{
		windowManager.addView("new-process-view");
	});
	
	function KeyValueBean(key, value)
	{
		this.key = key;
		this.value = value;
	}
 
	function startProcess(bpmDefinitionKey, processSimpleAttributes)
	{
		windowManager.showLoadingScreen();
		
		var jsonAttributes = "[{}]";
		
		if(processSimpleAttributes)
		{
			var processData = [];
			
			$.each(processSimpleAttributes, function( key, value )
			{
				var keyValueBean = new KeyValueBean(key, value);
				processData.push(keyValueBean);
			});
			jsonAttributes = JSON.stringify(processData, null, 2);
		}
		
		var widgetJson = $.getJSON('<spring:url value="/processes/startNewProcess.json"/>', 
		{
			"bpmDefinitionId": bpmDefinitionKey,
			"processSimpleAttributes": jsonAttributes
		})
		.done(function(data) 
		{ 
			<!-- Errors handling -->
			windowManager.clearErrors();
			
			var errors = [];
			$.each(data.errors, function() {
				errors.push(this);
				windowManager.addError(this.message);
			});
			
			if(errors.length > 0) { return; }
			
			if (data.taskId!=null) {
				console.log( "processStateConfigurationId: "+data.processStateConfigurationId ); 
				loadProcessView(data.processStateConfigurationId, data.taskId);
			} else {
				windowManager.showNewProcessPanel();
			}
			reloadQueues();

		});
    }
 
 </script>