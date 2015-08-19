<!-- Aperte Workflow Process Instance Manager -->
<!-- @author: lgajowy@bluesoft.net.pl -->

<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@include file="../../utils/globals.jsp" %>

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
                <th style="width:7%;"><spring:message code="processinstances.console.processInternalId.title"/></th>
                <th style="width:18%;"><spring:message code="processinstances.console.definitionsColumn.title"/></th>
                <th style="width:10%;"><spring:message code="processinstances.console.externalKey.title"/></th>
                <th style="width:5%;"><spring:message code="processinstances.console.history.createdby"/></th>
                <th style="width:10%;"><spring:message code="processinstances.console.history.on"/></th>
                <th style="width:15%;"><spring:message code="processinstances.console.taskName.title"/></th>
                <th style="width:13%;"><spring:message code="processinstances.console.entry.owner"/></th>
                <th style="width:7%;"><spring:message code="processinstances.console.entry.state"/></th>
                <th style="width:15%;"><spring:message code="processinstances.console.entry.available-actions"/></th>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>

<!-- User Changing Modal -->
<div class="modal fade" id="changeUserModal" tabindex="-1" role="dialog" aria-labelledby="categoryModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="categoryModalLabel"><spring:message code="processinstances.console.entry.change.owner.title" /></h4>
            </div>

            <div class="modal-body">
                <div class="panel panel-default">
                    <div class="panel-body">
                        <div class="form-horizontal">
                            <div class="form-group input-group-sm">
                                <input id="changeUserInput"  class="col-sm-10" style="width:100%"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" id="cancelChangeButton" class="btn btn-default" data-dismiss="modal">
                    <spring:message code="processinstances.console.cancel.process.button.negative" />
                </button>
                <button type="button" id="changeUserButton" class="btn btn-primary" data-dismiss="modal">
                    <spring:message code="processinstances.console.cancel.process.button.affirmative" />
                </button>
            </div>
        </div>
    </div>
</div><!-- /.modal -->


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
                { "sName":"creationDate", "bSortable": true ,"mData": function(object){return moment(object.creationDate).format('DD-MM-YYYY, HH:mm:ss');}},
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
            addListItem(actionList, "<spring:message code="processinstances.console.entry.change.owner"/>", 'toggleChangeAssigneeModal(' + object.taskInternalId + ',\"' + object.assignedTo + '\")');
            addListItem(actionList, "<spring:message code="processinstances.console.entry.remove-owner"/>", 'removeAssignee(' + object.taskInternalId + ',\"' + object.assignedTo + '\")');
            var dropdown = wrapDropdownWithDiv(button, actionList);
            return $(dropdown).html();
        }

        function generateActionDropdownButton(object) {
            var button = createDropdownButton("<spring:message code="processinstances.console.entry.available-actions"/>");
            var actionList = createActionList(object);
            if (object.assignedTo != null) {
                for (var i=0; i< object.availableActions.length; i++) {
                    var action = object.availableActions[i].actionName;
                    var actionLable = object.availableActions[i].actionTitle;
                    addListItem(actionList, actionLable, 'performActionForTask(' + object.taskInternalId + ',\"' + action + '\")');
                }
            } else {
                addErrorListItem(actionList, "<spring:message code="processinstances.console.noUserAssinged"/>");
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
            var centerText = document.createElement('center');
            $(centerText).text(message);
            var li = document.createElement('li');
            $(li).attr('class', 'list-group-item list-group-item-danger');
            li.appendChild(centerText);
            list.appendChild(li);
        }

        var currentlyChangedTaskId = null;
        var currentlyChangedTaskOldUserName = null;

        function toggleChangeAssigneeModal(taskId, oldUserName) {
            $('#changeUserModal').modal();
            currentlyChangedTaskId = taskId;
            currentlyChangedTaskOldUserName = oldUserName;
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
            if(confirm("<spring:message code="processinstances.console.cancel.process.confirm.message"/>")) {
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

        function removeAssignee(taskId, oldUserName) {
            if(confirm("<spring:message code="processinstances.console.remove.process.confirm.message"/>")) {
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

            $("#changeUserInput").select2({
                ajax: {
                    url: dispatcherPortlet,
                    dataType: 'json',
                    quietMillis: 200,
                    data: function (term, page) {
                        return {
                            q: term, // search term
                            page_limit: 10,
                            controller: "usercontroller",
                            page: page,
                            action: "getAllUsers"
                        };
                    },
                    results: function (data, page)
                    {
                        var results = [];
                      $.each(data.data, function(index, item)
                      {
                        if('${user.getLogin()}' != item.login)
                        {
                            results.push({
                              id: item.login,
                              text: item.realName + ' ['+item.login+']'
                            });

                        }
                      });
                      return {
                          results: results
                      };
                    }
                },
            });

            $("#changeUserButton").on('click', function() {
                var userFullName = $('.select2-hidden-accessible').text();
                var userLogin = userFullName.substring(userFullName.lastIndexOf("[")+1,userFullName.lastIndexOf("]"))
                modifyAssignee(currentlyChangedTaskId, currentlyChangedTaskOldUserName, userLogin);
                currentlyChangedTaskId = null;
                currentlyChangedTaskOldUserName = null;
            });
        });
    //]]>
</script>
