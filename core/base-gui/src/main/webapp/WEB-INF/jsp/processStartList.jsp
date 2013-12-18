
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ page import="pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig" %>
<%@ page import="pl.net.bluesoft.rnd.processtool.ui.jsp.ProcessStartList" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %>

<div id="new-process-view" class="new-process-block" hidden="true">
	<div class="process-queue-name">
		<spring:message code="new.process.view.header" />
	</div>

<%
List<ProcessDefinitionConfig> processStartList = (List)request.getAttribute("processStartList");
Map<String, List<ProcessDefinitionConfig>> groups = ProcessStartList.getGroupedConfigs(processStartList, request.getLocale());

for (Map.Entry<String, List<ProcessDefinitionConfig>> group : groups.entrySet()) {
	String groupName = group.getKey();
	List<ProcessDefinitionConfig> processList = group.getValue();

	if (groupName != null) {
%>
	<div class="process-start-list-group"><%= groupName %></div>
<%
	}
	else if (groups.size() > 1) {
%>
	<div class="process-start-list-group"><spring:message code='process.group.other' /></div>
<%
	}
%>

	<c:forEach var="processStart" items="<%= processList %>">
		<div class="process-start-list-row" onclick="startProcess('${processStart.bpmDefinitionKey}') ">
			<div id="${processStart.bpmDefinitionKey}" class="process-start-name" data-toggle="tooltip"
				title="<spring:message code='${processStart.description}' />  v. ${processStart.bpmDefinitionVersion}">
				<a class="process-start-link"><spring:message code="${processStart.processName}" /></a>
			</div>
			<div><spring:message code="${processStart.comment}" /></div>
		</div>
	</c:forEach>

<%
}
%>

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
		
		var widgetJson = $.getJSON('<portlet:resourceURL id="startNewProcess"/>',
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