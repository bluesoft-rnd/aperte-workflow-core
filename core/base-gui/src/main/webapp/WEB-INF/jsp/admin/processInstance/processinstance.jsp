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


<div class="process-tasks-view" id="foundProcessInstances" style="z-index: 1">
    <table id="processInstanceTable" class="process-table table table-striped" border="1">
        <thead>
                <th style="width:10%;">Definition name:</th>
                <th style="width:10%;">Task name</th>
                <th style="width:10%;">creator Login:</th>
                <th style="width:10%;">created on:</th>
                <th style="width:10%;">status:</th>
                <th style="width:10%;">Assigned to:</th>
                <th style="width:10%;">Actions</th>
                <th style="width:10%;">internal id</th>
                <th style="width:5%;">external key</th>
                <th style="width:15%;">dropdown</th>
        </thead>
        <tbody></tbody>
    </table>
</div>


<script type="text/javascript">
    //<![CDATA[
        var waitTime = 1000;
        var timeout;
        var dataTable = new AperteDataTable("processInstanceTable",
            [
                { "sName":"definitionName", "bSortable": true , "mData": "definitionName"},
                { "sName":"taskName", "bSortable": true , "mData": "taskName" },
                { "sName":"creatorLogin", "bSortable": true , "mData": "creatorLogin"},
                { "sName":"creationDate", "bSortable": true ,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm:ss');}},
                { "sName":"status", "bSortable": true , "mData": "status"},
                { "sName":"assignedTo", "bSortable": true , "mData": "assignedTo"},
                { "sName":"availableActions", "bSortable": true , "mData": "availableActions" },
                { "sName":"internalId", "bSortable": true , "mData": "internalId" },
                { "sName":"externalKey", "bSortable": true , "mData": "externalKey" },
                { "sName":"availableActions", "bSortable": true , "mData": function(object){return generateDropdownButton(object);}}
            ],
            [[ 1, "desc" ]]
        );

        setSearchParameters();

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

            $("#only_active").change(function() {
                performSearch();
            });
        });

        function doneTyping() {
            if (!timeout){
                return;
            }
            timeout = null;
            performSearch();
        }

        function performSearch() {
            setSearchParameters();
            dataTable.reloadTable(dispatcherPortlet);
        }

        function setSearchParameters() {
            dataTable.setParameters(
                [
                    { "name": "controller", "value" : "processInstanceController"},
                    { "name": "action", "value" : "findProcessInstances" },
                    { "name": "filter", "value" : document.getElementById("search_field").value },
                    { "name": "onlyActive", "value" : document.getElementById("only_active").checked}
                ]
            );
        }

        function generateDropdownButton(object) {
            console.log(object.availableActions)

            var dropdownDiv = document.createElement('div');
            dropdownDiv.className = 'btn-group';
            dropdownDiv.appendChild(


            // 1. create javascript  element,
            //2. append it to page!
            var listCode = ['<ul id="actionList" class="dropdown-menu" role="menu">'];

            for(var i = 0; i < object.availableActions.length; i++)
            {
                listCode.push('<li><a href="#">' + object.availableActions[i] + '</a></li>');
            }
            listCode.push('</ul>');

            var buttonCode =
                ['<div class="btn-group">',
                    '<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">',
                    'Action <span class="caret"></span>',
                   '</button>'];
            buttonCode.push(listCode);
            buttonCode.push('</div>');
            buttonCode.join('\n');
            return buttonCode;
        }
    //]]>
</script>