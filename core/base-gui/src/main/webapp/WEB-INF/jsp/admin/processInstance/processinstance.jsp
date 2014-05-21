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
                <th style="width:7%;">Process internal id</th>
                <th style="width:18%;">Definition:</th>
                <th style="width:10%;">external key</th>
                <th style="width:5%;"><spring:message code="processinstances.console.history.createdby"/></th>
                <th style="width:10%;"><spring:message code="processinstances.console.history.on"/></th>
                <th style="width:15%;">Task name</th>
                <th style="width:13%;"><spring:message code="processinstances.console.entry.owner"/></th>
                <th style="width:7%;"><spring:message code="processinstances.console.entry.state"/></th>
                <th style="width:15%;"><spring:message code="processinstances.console.entry.available-actions"/></th>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>

<script type="text/javascript">
    //<![CDATA[
        var waitTime = 500;
        var timeout;
        var dataTable = new AperteDataTable("processInstanceTable",
            [
                { "sName":"processInternalId", "bSortable": true , "mData": "processInternalId" },
                { "sName":"definitionName", "bSortable": true , "mData": function(object) {return formatDefinitionName(object);}},
                { "sName":"externalKey", "bSortable": true , "mData": "externalKey" },
                { "sName":"creatorLogin", "bSortable": true , "mData": "creatorLogin"},
                { "sName":"creationDate", "bSortable": true ,"mData": function(object){return $.format.date(object.creationDate, 'dd-MM-yyyy, HH:mm:ss');}},
                { "sName":"taskName", "bSortable": true , "mData": "taskName" },
                { "sName":"assignedTo", "bSortable": true , "mData": function(object){return generateAssignedUserDropdown(object);}},
                { "sName":"status", "bSortable": true , "mData": function(object){return taskState(object);}},
                { "sName":"availableActions", "bSortable": true , "mData": function(object){ return generateActionDropdownButton(object); }}
            ],
            [[ 1, "desc" ]]
        );

        setSearchParameters();

        function taskState(object) {
            return object.taskName + " " + object.taskInternalId;
        }

        function formatDefinitionName(object) {
            return object.definitionDescription + " (Def Id: " +  object.definitionId + ") " + object.bpmDefinitionKey;
        }

        function generateAssignedUserDropdown(object) {
            var button = createDropdownButton((object.assignedTo != null) ? object.assignedTo : "<spring:message code="processinstances.console.entry.no-owner"/>");
            var actionList = createActionList();
            addListItem(actionList, "<spring:message code="processinstances.console.entry.change.owner"/>", 'changeAssignee(' + object.taskInternalId + ',\"' + object.assignedTo + '\")');
            addListItem(actionList, "<spring:message code="processinstances.console.entry.remove-owner"/>", 'removeAssignee(' + object.taskInternalId + ',\"' + object.assignedTo + '\")');
            var dropdown = wrapDropdownWithDiv(button, actionList);
            return $(dropdown).html();
        }

        function generateActionDropdownButton(object) {
            var button = createDropdownButton("<spring:message code="processinstances.console.entry.available-actions"/>");
            var actionList = createActionList(object);

            if (object.assignedTo != null) {
                for (var i=0; i< object.availableActions.length; i++) {
                    var actionName = object.availableActions[i];
                    addListItem(actionList, actionName, 'performActionForTask(' + object.taskInternalId + ',\"' + actionName + '\")');
                }
            } else {
                addErrorListItem(actionList, "No user assigned");
            }
            addListItem(actionList, "<spring:message code="processinstances.console.cancel-process"/>", 'cancelProcessInstance(' + object.processInternalId + ')');
            return $(wrapDropdownWithDiv(button, actionList)).html();
        }

        function wrapDropdownWithDiv(button, actionList) {
            var div = document.createElement('div');
            $(div).attr('class', 'btn-group');
            div.appendChild(button);
            div.appendChild(actionList);
            var dropdownButton = document.createElement('div');
            dropdownButton.appendChild(div);
            return dropdownButton;
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

        function createActionList() {
            var actionList = document.createElement('ul');
            $(actionList).attr('id', 'actionList');
            $(actionList).attr('class', 'dropdown-menu');
            $(actionList).attr('role', 'menu');
            return actionList;
        }

        function addListItem(list, title, onClickAction) {
            var a = document.createElement('a');
            $(a).attr('nohref');
            $(a).attr('onclick', onClickAction);

            a.innerHTML = title;
            var li = document.createElement('li');
            li.appendChild(a);
            list.appendChild(li);
        }

        function addErrorListItem(list, message) {
            var li = document.createElement('li');
            $(li).attr('class', 'list-group-item list-group-item-danger');
            $(li).text(message);
            list.appendChild(li);
        }

        function performActionForTask(taskId, action) {
           if (confirm("<spring:message code="processinstances.console.force-action.confirm.title"/>")) {
               ajaxPost({
                   controller : 'processInstanceController',
                   action : 'performAction',
                   taskInternalId : taskId,
                   actionToPerform : action },
                   function(response) {
                        alert("<spring:message code="processinstances.console.force-action.success"/>");
                        dataTable.reloadTable(dispatcherPortlet);
                   }
               );
           }
        }

        function cancelProcessInstance(processId) {
            if(confirm("Are you sure?")) {
                ajaxPost({
                    controller : 'processInstanceController',
                    action : 'cancelProcessInstance',
                    processInstanceId : processId },
                    function(response) {
                        dataTable.reloadTable(dispatcherPortlet);
                        alert("<spring:message code="processinstances.console.cancel-process.success"/>");
                });
            }
        }


        function changeAssignee(taskId, oldUserName) {
            var userLogin = prompt("Please enter user login","");   //TODO - zrobic select list!
            modifyAssignee(taskId, oldUserName, userLogin);
        }

        function removeAssignee(taskId, oldUserName) {
            if(confirm("Are you sure?")) {
                modifyAssignee(taskId, oldUserName);
            }
        }

        function modifyAssignee(taskId, oldUserName, newUserName) {
            ajaxPost({
                controller : 'processInstanceController',
                action : 'modifyTaskAssignee',
                taskInternalId : taskId,
                oldUserLogin : oldUserName,
                newUserLogin : newUserName },
                function(response) { dataTable.reloadTable(dispatcherPortlet);
            });
        }

        function ajaxPost(settings, doneAction) {
            $.ajax({
                url : dispatcherPortlet,
                type : "POST",
                data : settings
                }).done(doneAction());
        }

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

    //]]>
</script>
