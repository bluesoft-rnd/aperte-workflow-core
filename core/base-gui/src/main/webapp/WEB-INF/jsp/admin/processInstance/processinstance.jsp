<!-- Aperte Workflow Substitution Manager -->
<!-- @author: lgajowy@bluesoft.net.pl -->

<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@include file="../../utils/globals.jsp" %>
<%@include file="../../utils/apertedatatable.jsp" %>

<div class="process-queue-name apw_highlight">
    Aperte Workflow Process Instances
</div>

<div id="criteria-input-field">
    <h1><spring:message code="processinstances.console.title" /></h1>
    <div class="form-group input-group-sm">
        <h3>Enter filter criteria</h3>
        <input type="text" id="search_field" class="col-sm-10" style="width:100%"/>
        <input type="checkbox" id="only_active" checked><spring:message code="processinstances.search.onlyActive"/></input>
    </div>
</div>



<div class="process-tasks-view" id="task-view-processes">
    <table id="processInstanceTable" class="process-table table table-striped" border="1">
        <thead>
                <th style="width:20%;">table title</th>
                <th style="width:20%;"><spring:message code="admin.substitution.table.substituting" /></th>
                <th style="width:20%;"><spring:message code="admin.substitution.table.dateFrom" /></th>
                <th style="width:20%;"><spring:message code="admin.substitution.table.dateTo" /></th>
                <th style="width:20%;"><spring:message code="admin.substitution.table.action" /></th>
        </thead>
        <tbody></tbody>
    </table>
</div>



<script type="text/javascript">
    //<![CDATA[
        var waitTime = 2000;
        var timeout;

      	$(document).ready(function()
    	{
            $('#search_field').on('input',function() {
                var el = this;

                if (timeout) clearTimeout(timeout);
                timeout = setTimeout(function() {
                    doneTyping.call(el);
                }, waitTime);
            });
                $('#search_field').blur(function(){
                    doneTyping.call(this);
                });
        });

        function doneTyping() {
            if (!timeout){
                return;
            }
            timeout = null;

            var dataTable = new AperteDataTable("processInstanceTable",
                [
                    { "sName":"userLogin", "bSortable": true , "mData": "userLogin"},
                    { "sName":"userSubstituteLogin", "bSortable": true ,"mData": "userSubstituteLogin"},
                    { "sName":"dateFrom", "bSortable": true ,"mData": function(object){return $.format.date(object.dateFrom, 'dd-MM-yyyy, HH:mm:ss');}},
                    { "sName":"dateTo", "bSortable": true ,"mData": function(object){return $.format.date(object.dateTo, 'dd-MM-yyyy, HH:mm:ss');}},
                    { "sName":"action", "bSortable": true ,"mData": function(object){return "<div></div>";} }
                ],
                [[ 3, "desc" ]]
            );

            dataTable.addParameter("controller", "processInstanceController");
            dataTable.addParameter("action", "findProcessInstances");
            dataTable.reloadTable(dispatcherPortlet);

            //$.post(dispatcherPortlet,
            //{
            //    "controller": "processInstanceController",
            //    "action": "findProcessInstances"
            //}).done(function() {
            //   alert('done ajax');
            //});
        }
    //]]>
</script>