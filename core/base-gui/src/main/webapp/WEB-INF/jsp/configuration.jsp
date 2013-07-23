<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="configuration" hidden="true">
	<div class="toggle-buttons">
	<div class="process-queue-name">
		<spring:message code="configuration.process.table.header" /> 
	</div>
	<fieldset data-role="controlgroup">
		<label class="checkbox">
			<input id="button-processesTable-name" type="checkbox"  name="checkme0" onClick="toggleColumn(this, 'process', 'name');" /><spring:message code="processes.button.hide.processname" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-step" type="checkbox"  name="checkme1" onClick="toggleColumn(this, 'process','step');" /><spring:message code="processes.button.hide.step" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-code" type="checkbox"  name="checkme2" onClick="toggleColumn(this, 'process','code');" /><spring:message code="processes.button.hide.processcode" />
        </label>
        <label class="checkbox">
            <input id="button-processesTable-creator" type="checkbox"  name="checkme3" onClick="toggleColumn(this, 'process','creator');" /> <spring:message code="processes.button.hide.creator" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-assignee" type="checkbox"  name="checkme4"  onClick="toggleColumn(this, 'process','assignee');"/> <spring:message code="processes.button.hide.assignee" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-creationDate" type="checkbox"  name="checkme5" onClick="toggleColumn(this, 'process','creationDate');" />  <spring:message code="processes.button.hide.creationdate" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-deadline" type="checkbox"  name="checkme6"  onClick="toggleColumn(this, 'process','deadline');" /><spring:message code="processes.button.hide.deadline" />
        </label>
	</fieldset>
	
	<div class="process-queue-name">
		<spring:message code="configuration.customqueues.table.header" /> 
	</div>
	<fieldset data-role="controlgroup">
		<label class="checkbox">
			<input id="button-customQueueTable-name" type="checkbox"  name="checkme0" onClick="toggleColumn(this, 'queue', 'name');" /><spring:message code="processes.button.hide.processname" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-step" type="checkbox"  name="checkme1" onClick="toggleColumn(this, 'queue','step');" /><spring:message code="processes.button.hide.step" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-code" type="checkbox"  name="checkme2" onClick="toggleColumn(this, 'queue','code');" /><spring:message code="processes.button.hide.processcode" />
        </label>
        <label class="checkbox">
            <input id="button-customQueueTable-creator" type="checkbox"  name="checkme3" onClick="toggleColumn(this, 'queue','creator');" /> <spring:message code="processes.button.hide.creator" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-creationDate" type="checkbox"  name="checkme5" onClick="toggleColumn(this, 'queue','creationDate');" />  <spring:message code="processes.button.hide.creationdate" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-deadline" type="checkbox"  name="checkme6"  onClick="toggleColumn(this, 'queue','deadline');" /><spring:message code="processes.button.hide.deadline" />
        </label>
		<label class="checkbox">
			<input id="button-customQueueTable-actions" type="checkbox"  name="checkme4"  onClick="toggleColumn(this, 'queue','actions');"/> <spring:message code="processes.button.hide.actions" />
        </label>
	</fieldset>
	</div>
</div>

<script type="text/javascript">
  	$(document).ready(function()
	{
		windowManager.addView("configuration");
	});
	
	function toggleColumn(button, viewName, columnName)
	{
		queueViewManager.toggleColumn(viewName, columnName);
	}	
	
	

</script>