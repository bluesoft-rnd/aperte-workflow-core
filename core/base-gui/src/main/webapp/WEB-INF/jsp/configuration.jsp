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
			<input id="button-processesTable-name" type="checkbox" value="name" name="process" onClick="toggleColumn(this, 'process', 'name');" /><spring:message code="processes.button.hide.processname" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-step" type="checkbox" value="step" name="process" onClick="toggleColumn(this, 'process','step');" /><spring:message code="processes.button.hide.step" />
        </label>
		<label class="checkbox">
			<input id="button-processesTable-businessStatus" type="checkbox" value="businessStatus" name="process" onClick="toggleColumn(this, 'process','businessStatus');" /><spring:message code="processes.button.hide.businessStatus" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-code" type="checkbox" value="code" name="process" onClick="toggleColumn(this, 'process','code');" /><spring:message code="processes.button.hide.processcode" />
        </label>
        <label class="checkbox">
            <input id="button-processesTable-creator" type="checkbox"  value="creator" name="process" onClick="toggleColumn(this, 'process','creator');" /> <spring:message code="processes.button.hide.creator" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-assignee" type="checkbox" value="assignee" name="process"  onClick="toggleColumn(this, 'process','assignee');"/> <spring:message code="processes.button.hide.assignee" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-creationDate" type="checkbox" value="creationDate" name="process" onClick="toggleColumn(this, 'process','creationDate');" />  <spring:message code="processes.button.hide.creationdate" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-deadline" type="checkbox" value="deadline" name="process"  onClick="toggleColumn(this, 'process','deadline');" /><spring:message code="processes.button.hide.deadline" />
        </label>
        <label class="checkbox">
			<input id="button-processesTable-stepInfo" type="checkbox" value="stepInfo" name="process"  onClick="toggleColumn(this, 'process','stepInfo');" /><spring:message code="processes.button.hide.stepInfo" />
		</label>
	</fieldset>
	
	<div class="process-queue-name">
		<spring:message code="configuration.customqueues.table.header" /> 
	</div>
	<fieldset data-role="controlgroup">
		<label class="checkbox">
			<input id="button-customQueueTable-name" type="checkbox" value="name"  name="queue" onClick="toggleColumn(this, 'queue', 'name');" /><spring:message code="processes.button.hide.processname" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-step" type="checkbox" value="step" name="queue" onClick="toggleColumn(this, 'queue','step');" /><spring:message code="processes.button.hide.step" />
        </label>
		<label class="checkbox">
			<input id="button-customQueueTable-businessStatus" type="checkbox" value="businessStatus" name="queue" onClick="toggleColumn(this, 'queue','businessStatus');" /><spring:message code="processes.button.businessStatus.step" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-code" type="checkbox" value="code" name="queue" onClick="toggleColumn(this, 'queue','code');" /><spring:message code="processes.button.hide.processcode" />
        </label>
        <label class="checkbox">
            <input id="button-customQueueTable-creator" type="checkbox" value="creator" name="queue" onClick="toggleColumn(this, 'queue','creator');" /> <spring:message code="processes.button.hide.creator" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-creationDate" type="checkbox" value="creationDate" name="queue" onClick="toggleColumn(this, 'queue','creationDate');" />  <spring:message code="processes.button.hide.creationdate" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-deadline" type="checkbox" value="deadline" name="queue"  onClick="toggleColumn(this, 'queue','deadline');" /><spring:message code="processes.button.hide.deadline" />
        </label>
        <label class="checkbox">
			<input id="button-customQueueTable-stepInfo" type="checkbox" value="stepInfo" name="queue"  onClick="toggleColumn(this, 'queue','stepInfo');" /><spring:message code="processes.button.hide.stepInfo" />
		</label>
		<label class="checkbox">
			<input id="button-customQueueTable-actions" type="checkbox" value="actions" name="queue"  onClick="toggleColumn(this, 'queue','actions');"/> <spring:message code="processes.button.hide.actions" />
        </label>
	</fieldset>
	</div>
</div>

<script type="text/javascript">
  	$(document).ready(function()
	{
		windowManager.addView("configuration");
		loadProcessesCheckboxes();
		loadCustomQueueCheckboxes();
		
	});
	
	function loadProcessesCheckboxes(queueName){
		var loadedProcess = loadCookies("process");
		var parsedProcess = parseCookie(loadedProcess);
		setProcessCheckboxes(parsedProcess);
	}
	
	function loadCustomQueueCheckboxes(){
		var loadedCustom = loadCookies("queue");
		var parsedCustom = parseCookie(loadedCustom);
		setCustomQueueCheckboxes(parsedCustom);
	}
	
	function setProcessCheckboxes(parsedProcess){
		setCheckboxes(parsedProcess,"processes");
	}
	function setCustomQueueCheckboxes(parsedProcess){
		setCheckboxes(parsedProcess,"customQueue");
	}
	
	function setCheckboxes(parsedProcess,queueName){
		var keys = Object.keys(parsedProcess);
		$.each(keys, function(index, value) {
			selectProcessTableCheckBox(value,parsedProcess[value],queueName)
		});	
	}
	
	function selectProcessTableCheckBox(name,value,queueName){
		$("#button-"+queueName+"Table-"+name).attr("checked", value);
	}

	function toggleColumn(button, viewName, columnName)
	{
		var list = $('input:checkbox[name='+viewName+']').map(function() {
			return '"'+$(this).val() +'":'+ $(this).is(":checked");
		}).get().join();
		list = "{"+list+"}";
		saveProcessCookie(list);
		queueViewManager.toggleColumn(viewName, columnName);
	}
</script>