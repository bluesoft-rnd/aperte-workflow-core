<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@include file="utils/globals.jsp" %>
<%@include file="utils/apertedatatable.jsp" %>

<div class="process-tasks-view" id="case-management-view">
    <table id="caseManagementTable" class="process-table table table-striped" border="1">
        <thead>
                <th style="width:20%;"><spring:message code="admin.case.management.results.table.number" /></th>
                <th style="width:20%;"><spring:message code="admin.case.management.results.table.definitionName" /></th>
                <th style="width:20%;"><spring:message code="admin.case.management.results.table.name" /></th>
                <th style="width:20%;"><spring:message code="admin.case.management.results.table.currentStageName" /></th>
                <th style="width:20%;"><spring:message code="admin.case.management.results.table.createDate" /></th>
                <th style="width:20%;"><spring:message code="admin.case.management.results.table.modificationDate" /></th>
        </thead>
        <tbody></tbody>
    </table>
</div>


<script type="text/javascript">
//<![CDATA[
    var caseManagement = {}

  	$(document).ready(function() {
        caseManagement.caseListDT = new AperteDataTable("caseManagementTable",
            [
                 { "sName":"number", "bSortable": true ,"mData": function(object) { return caseManagement.generateNameColumn(object) }
                 },
                 { "sName":"definitionName", "bSortable": false ,"mData": "definitionName"},
                 { "sName":"name", "bSortable": true , "mData": "name"},
                 { "sName":"currentStageName", "bSortable": false ,"mData": "currentStageName"},
                 { "sName":"createDate", "bSortable": true ,"mData": "createDate"},
                 { "sName":"modificationDate", "bSortable": true ,"mData": "modificationDate"}
            ],
            [[ 0, "asc" ]]
        );

        caseManagement.caseListDT.addParameter("controller", "casemanagementcontroller");
        caseManagement.caseListDT.addParameter("action", "getAllCasesPaged");
        // if (window.console) console.log(dispatcherPortlet);
        caseManagement.caseListDT.reloadTable(dispatcherPortlet);

    });

	caseManagement.generateNameColumn = function(caseInstance) {
        // if (window.console) console.log(caseInstance);
        var showOnClickCode = 'onclick="caseManagement.loadCaseView(' + caseInstance.id + ')"';
        // if (window.console) console.log(showOnClickCode);
        return '<a class="process-view-link"  '+ showOnClickCode + ' >' + caseInstance.number + '</a>';
    }

    caseManagement.loadCaseView = function(caseId) {
        windowManager.changeUrl('?caseId=' + caseId);
        windowManager.showLoadingScreen();

        var widgetJson = $.post(dispatcherPortlet, {
                "controller": "casemanagementcontroller",
                "action": "loadCase",
                "caseId" : caseId
            })
            .done(function(data) {
                if (window.console)
                    console.log(data);
                clearAlerts();
                windowManager.showProcessData();
                $('#process-data-view').empty();
                $("#process-data-view").append(data);
                checkIfViewIsLoaded();
            })
            .fail(function(data, textStatus, errorThrown) {
            }
        );
    }

//]]>
</script>
