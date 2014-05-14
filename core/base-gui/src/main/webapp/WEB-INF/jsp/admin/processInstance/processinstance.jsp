<!-- Aperte Workflow Process Instance Manager -->
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
                <th style="width:15%;">Definition:</th>
                <th style="width:15%;">Task name</th>
                <th style="width:5%;">creator Login:</th>
                <th style="width:5%;">created on:</th>
                <th style="width:5%;">status:</th>
                <th style="width:15%;">Assigned to:</th>
                <th style="width:10%;">internal id</th>
                <th style="width:15%;">Actions</th>
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
                { "sName":"definitionName", "bSortable": true , "mData": function(object) {return formatDefinitionName(object);}},
                { "sName":"taskName", "bSortable": true , "mData": "taskName" },
                { "sName":"creatorLogin", "bSortable": true , "mData": "creatorLogin"},
                { "sName":"creationDate", "bSortable": true ,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm:ss');}},
                { "sName":"status", "bSortable": true , "mData": "status"},
                { "sName":"assignedTo", "bSortable": true , "mData": function(object){return generateAssignedUserButtonGroup(object);}},
                { "sName":"internalId", "bSortable": true , "mData": "internalId" },
                { "sName":"availableActions", "bSortable": true , "mData": function(object){return generateActionDropdownButton(object);}}
            ],
            [[ 1, "desc" ]]
        );

        setSearchParameters();

        function formatDefinitionName(object) {
            return object.definitionDescription + " (Def Id: " +  object.definitionId + ") " + object.bpmDefinitionKey;
        }

        function generateAssignedUserButtonGroup(object) {
            var changeButton = document.createElement('button');
            $(changeButton).attr('type', 'button');
            $(changeButton).attr('class', 'btn btn-default');
            changeButton.textContent = "Change";

            var removeButton = document.createElement('button');
            $(removeButton).attr('type', 'button');
            $(removeButton).attr('class', 'btn btn-default');
            removeButton.textContent = "Remove";

            var label = document.createElement('label');
            $(label).text(object.assignedTo);

            var buttonGroup = document.createElement('div');
            $(buttonGroup).attr('class', 'btn-group btn-group-xs');
            buttonGroup.appendChild(label);
            buttonGroup.appendChild(changeButton);
            buttonGroup.appendChild(removeButton);

            var div = document.createElement('div');
            div.appendChild(buttonGroup);
            return $(div).html();
        }



        function generateActionDropdownButton(object) {
            var button = createDropdownButton("Available actions");
            var actionList = createActionList(object);
            var div = document.createElement('div');
            $(div).attr('class', 'btn-group');
            div.appendChild(button);
            div.appendChild(actionList);

            var dropdownButton = document.createElement('div');
            dropdownButton.appendChild(div);
            return $(dropdownButton).html();
        }

        function createDropdownButton(title) {
            var button = document.createElement('button');
            $(button).attr('type', 'button');
            $(button).attr('class', 'btn btn-default dropdown-toggle');
            $(button).attr('data-toggle', 'dropdown');
            button.textContent = title;
            var caret = document.createElement('span');
            $(caret).attr('class', 'caret');
            button.appendChild(caret);
            return button;
        }

        function createActionList(object) {
            var actionList = document.createElement('ul');
            $(actionList).attr('id', 'actionList');
            $(actionList).attr('class', 'dropdown-menu');
            $(actionList).attr('role', 'menu');

            for (var i=0; i< object.availableActions.length; i++) {
                addListItem(actionList, object.availableActions[i], object);
            }

            addListItem(actionList, "Cancel", 'cancelProcessInstance('+object.internalId+')');
            return actionList;
        }

        function addListItem(list, title, onClickAction) {
            var a = document.createElement('a');
            $(a).attr('nohref');
            $(a).attr('onclick', onClickAction);
            a.textContent = title;
            var li = document.createElement('li');
            li.appendChild(a);
            list.appendChild(li);
        }

        function cancelProcessInstance(processId) {
            if(confirm("Are you sure?")) {
                ajaxPost({
                    controller : 'processInstanceController',
                    action : 'cancelProcessInstance',
                    processInstanceId : processId },
                    function(response) { dataTable.reloadTable(dispatcherPortlet);
                });
            }
        }

        function ajaxPost(settings, doneAction) {
            $.ajax({
                url : dispatcherPortlet,
                type : "POST",
                data : settings
                }).done(doneAction());
        }

      	$(document).ready(function()
    	{
            $('#search_field').on('input',function() {
                var el = this;
                if (timeout) clearTimeout(timeout);
                timeout = setTimeout(function() {
                    doneTyping.call(el);
                }, waitTime);
            }).on('blur', function(){
                doneTyping.call(this);
            });

            $("#only_active").on('change', function() {
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
    //]]>
</script>