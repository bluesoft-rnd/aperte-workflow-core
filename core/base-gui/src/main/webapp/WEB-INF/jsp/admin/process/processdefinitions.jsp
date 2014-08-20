<!-- Aperte Workflow Process Definitions -->
<!-- @author: mkrol@bluesoft.net.pl -->

<%@ page
	import="org.springframework.web.servlet.support.RequestContextUtils"
	import="pl.net.bluesoft.rnd.processtool.dao.ProcessDefinitionDAO"
	import="pl.net.bluesoft.rnd.processtool.ProcessToolContext"
	import="java.util.List"
	import="pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig"
	import="java.util.ArrayList"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@include file="../../utils/globals.jsp"%>
<%@include file="../../utils/apertedatatable.jsp"%>

<!-- Modal -->
<div class="modal fade" id="newSubstitutionModal" tabindex="-1"
	role="dialog" aria-labelledby="categoryModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h4 class="modal-title" id="categoryModalLabel">
					<spring:message code="admin.substitution.modal.add.title" />
				</h4>
			</div>
			<div class="modal-body">
				<div class="form-horizontal">
					<div class="form-group input-group-sm">
						<label name="tooltip"
							title="<spring:message code='substituting.user.label.tooltip' />"
							class="col-sm-2 control-label"><spring:message
								code="substituting.user.label" /></label> <input id="substitute-user"
							class="col-sm-10"
							data-placeholder="<spring:message code='substituting.user.input.placeholder' />" />
					</div>

					<div class="form-group input-group-sm">
						<label name="tooltip"
							title="<spring:message code='substitute.user.label.tooltip' />"
							for="substitute-user" class="col-sm-2 control-label required"><spring:message
								code="substitute.user.label" /></label> <input id="substitute-user"
							class="col-sm-10"
							data-placeholder="<spring:message code='substituting.user.input.placeholder' />" />
					</div>

					<div class="form-group input-group-sm">
						<label name="tooltip"
							title="<spring:message code='substituting.date.from.tooltip' />"
							for="substituting-date-from"
							class="col-sm-2 control-label required"><spring:message
								code="substituting.date.from.label" /></label>
						<div class="col-sm-4">
							<div class="input-group input-group-sm  input-append date">
								<input type="text" class="form-control"
									data-placeholder="<spring:message code='substituting.user.datefrom.placeholder' />"
									id="substituting-date-from"> <span
									class="input-group-addon"><i
									class="glyphicon glyphicon-calendar"></i></span>
							</div>
						</div>
					</div>

					<div class="form-group input-group-sm">
						<label name="tooltip"
							title="<spring:message code='substituting.date.to.tooltip' />"
							for="substituting-date-to"
							class="col-sm-2 control-label required"><spring:message
								code="substituting.date.to.label" /></label>
						<div class="col-sm-4">
							<div class="input-group input-group-sm  input-append date">
								<input type="text" class="form-control"
									id="substituting-date-to" /> <span class="input-group-addon"><i
									class="glyphicon glyphicon-calendar"></i></span>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Anuluj</button>
				<button type="button" class="btn btn-primary">Wybierz</button>
			</div>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>
<!-- /.modal -->

<div class="process-queue-name apw_highlight">
	Aperte Workflow Substitution Manager
	<div class="btn-group  pull-right">
		<button class="btn btn-info" id="substitution-add-button"
			data-target="#newSubstitutionModal" data-original-title="" title="">
			<span class="glyphicon glyphicon-plus"></span>
			<spring:message code="admin.substitution.action.add" />
		</button>
	</div>
</div>



<div class="process-tasks-view" id="task-view-processes">
	<table id="definitionsTable" class="process-table table table-striped"
		border="1">
		<thead>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.substituted" /></th>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.substituting" /></th>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.dateFrom" /></th>
			<th style="width: 20%;"><spring:message
					code="admin.substitution.table.dateTo" /></th>
			<th style="width: 20%;"><spring:message
					code="processdefinitions.console.version" /></th>
			<th style="width: 20%;"><spring:message
					code="processdefinitions.console.uploadedby" /></th>
			<th style="width: 20%;"><spring:message
					code="processdefinitions.console.uploadedon" /></th>
			<th style="width: 20%;"><spring:message
					code="processdefinitions.console.uploadedon" /></th>
		</thead>
		<tbody>

		</tbody>
	</table>
</div>


<script type="text/javascript">
	//<![CDATA[

	$(document).ready(function() {
		dataTable = new AperteDataTable("definitionsTable", [ {
			"sName" : "id",
			"bSortable" : true,
			"mData" : "id"
		}, {
			"sName" : "description",
			"bSortable" : true,
			"mData" : "description"
		}, {
			"sName" : "bpmDefinitionKey",
			"bSortable" : true,
			"mData" : "bpmDefinitionKey"
		}, {
			"sName" : "comment",
			"bSortable" : true,
			"mData" : "comment"
		}, {
			"sName" : "bpmDefinitionVersion",
			"bSortable" : true,
			"mData" : "bpmDefinitionVersion"
		}, {
			"sName" : "creatorLogin",
			"bSortable" : true,
			"mData" : "creatorLogin"
		}, {
			"sName" : "createDate",
			"bSortable" : true,
			"mData" : "createDate"
		}, {
			"sName" : "enabled",
			"bSortable" : true,
			"mData" : function(o){return '<input name="'+o.id+'" type="checkbox" checked="'+o.enabled+'" />';}
		} ], [ [ 3, "desc" ] ]);

		dataTable.addParameter("controller", "processDefinitionsController");
		dataTable.addParameter("action", "loadProcessDefinitions");
		dataTable.reloadTable(dispatcherPortlet);
		

		$("#definitionsTable input").on("click", function() {
		});

	});

	//]]>
</script>