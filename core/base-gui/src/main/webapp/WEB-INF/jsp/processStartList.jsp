<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="new-process-view" class="new-process-block">
	<div class="process-queue-name">
		<spring:message code="new.process.view.header" />
	</div>
	<c:forEach var="processStart" items="${processStartList}">
		<div class="process-start-list-row">
			<div id="${processStart.bpmDefinitionKey}" class="process-start-name" data-toggle="tooltip" title="<spring:message code='${processStart.comment}' /> "><a class="process-start-link" onclick="startProcess('${processStart.bpmDefinitionKey}') "><spring:message code="${processStart.processName}" /></a></div>
			<div><spring:message code="${processStart.comment}" /> </div>
		</div>
	</c:forEach>
</div>

 <script type="text/javascript">
 
	function startProcess(bpmDefinitionKey)
	{
		console.log( "processStart key:" + bpmDefinitionKey); 
		
		var widgetJson = $.getJSON('<spring:url value="/processes/startNewProcess.json"/>', 
		{
			"bpmDefinitionId": bpmDefinitionKey
		})
		.done(function(data) 
		{ 
			var taskId = data.taskId;
			var processStateConfigurationId = data.processStateConfigurationId;
			
			console.log( "processStateConfigurationId: "+processStateConfigurationId ); 
			
			loadProcessView(processStateConfigurationId, taskId);
			
			reloadQueues();

		})
		.fail(function() { console.log( "error" ); })
		.always(function() { console.log( "complete" ); });
    }
 
 </script>