<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="new-process-view" class="new-process-block">
	<c:forEach var="processStart" items="${processStartList}">
		<div class="process-start-list-row">
			<div id="${processStart.bpmDefinitionKey}" class="process-start-name"><a class="process-start-link" onclick="startProcess('${processStart.bpmDefinitionKey}') "><spring:message code="${processStart.processName}" /></a></div>
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

		})
		.fail(function() { console.log( "error" ); })
		.always(function() { console.log( "complete" ); });
    }
 
 </script>