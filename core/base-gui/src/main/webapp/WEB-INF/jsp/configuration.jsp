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
		<button id="process-table-hide-0" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(0);" ><spring:message code="processes.button.hide.processname" /></button>
		<button id="process-table-hide-1" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(1);" ><spring:message code="processes.button.hide.step" /></button>
		<button id="process-table-hide-1" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(2);" ><spring:message code="processes.button.hide.processcode" /></button>
		<button id="process-table-hide-2" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(3);" ><spring:message code="processes.button.hide.creator" /></button>
		<button id="process-table-hide-3" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(4);" ><spring:message code="processes.button.hide.assignee" /></button>
		<button id="process-table-hide-4" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(5);" ><spring:message code="processes.button.hide.creationdate" /></button>
		<button id="process-table-hide-5" type="button" class="btn mobile-button" data-toggle="button" onClick="toggleColumn(6);" ><spring:message code="processes.button.hide.deadline" /></button>
	</fieldset>
	</div>
</div>

<script type="text/javascript">

</script>