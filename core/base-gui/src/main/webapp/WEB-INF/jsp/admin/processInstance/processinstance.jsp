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

<div id="filter-criteria-input">
    <h1><spring:message code="processinstances.console.title" /></h1>
    <div class="form-group input-group-sm">
        <h3><spring:message code="processinstances.search.prompt"/></h3>
        <input type="text" id="search_field" class="col-sm-10" style="width:100%"><br>
        <input type="checkbox" id="only_active" checked><spring:message code="processinstances.search.onlyActive"/></input>
    </div>
</div>

<div class="process-tasks-view" id="foundProcessInstances">
    <table id="processInstanceTable" class="process-table table table-striped" border="1">
        <thead>
                <th style="width:10%;">Definition name:</th>
                <th style="width:10%;">creator Login:</th>
                <th style="width:10%;">created on:</th>
                <th style="width:10%;">status:</th>
                <th style="width:10%;">External key:</th>
                <th style="width:10%;">Internal Id:</th>
        </thead>
        <tbody></tbody>
    </table>
</div>

<div>


<script type="text/javascript">
    //<![CDATA[
        var waitTime = 1000;
        var timeout;
        var dataTable = new AperteDataTable("processInstanceTable",
            [
                { "sName":"definitionName", "bSortable": true , "mData": "definitionName"},
                { "sName":"creatorLogin", "bSortable": true , "mData": "creatorLogin"},
                { "sName":"creationDate", "bSortable": true ,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm:ss');}},
                { "sName":"status", "bSortable": true , "mData": "status"},
                { "sName":"externalKey", "bSortable": true , "mData": "externalKey"},
                { "sName":"internalId", "bSortable": true , "mData": "internalId"}
            ],
            [[ 1, "desc" ]]
        );
        dataTable.addParameter("controller", "processInstanceController");
        dataTable.addParameter("action", "findProcessInstances");

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
            dataTable.reloadTable(dispatcherPortlet);
        }
    //]]>
</script>