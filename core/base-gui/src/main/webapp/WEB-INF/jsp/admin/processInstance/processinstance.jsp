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
                <th style="width:20%;">Definition:</th>
                <th style="width:15%;">Task name</th>
                <th style="width:5%;">creator Login:</th>
                <th style="width:5%;">created on:</th>
                <th style="width:5%;">status:</th>
                <th style="width:5%;">Assigned to:</th>
                <th style="width:10%;">Actions</th>
                <th style="width:10%;">internal id</th>
                <th style="width:10%;">external key</th>
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
                { "sName":"definitionName", "bSortable": true , "mData": function(object) {return formatDefinitionName(object);}},
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

        function formatDefinitionName(object) {
            return object.definitionDescription + " (Def Id: " +  object.definitionId + ") " + object.bpmDefinitionKey;
        }

        function generateDropdownButton(object) {
                    var button = createButton();
                    var actionList = createActionList(object.availableActions);
                    var div = document.createElement('div');
                    $(div).attr('class', 'btn-group');
                    div.appendChild(button);
                    div.appendChild(actionList);

                    var dropdownButton = document.createElement('div');
                    dropdownButton.appendChild(div);
                    return $(dropdownButton).html();
                }

        function createButton() {
            var button = document.createElement('button');
            $(button).attr('type', 'button');
            $(button).attr('class', 'btn btn-default dropdown-toggle');
            $(button).attr('data-toggle', 'dropdown');
            button.textContent = 'AvailableActions';
            var caret = document.createElement('span');
            $(caret).attr('class', 'caret');
            button.appendChild(caret);
            return button;
        }


        // referencje do akcji wysłać przez object
        function createActionList(availableActions) {
            var actionList = document.createElement('ul');
            $(actionList).attr('id', 'actionList');
            $(actionList).attr('class', 'dropdown-menu');
            $(actionList).attr('role', 'menu');

            for (var i=0; i< availableActions.length; i++) {
                addListItem(actionList, availableActions[i], '#');
            }

            addListItem(actionList, 'Show process map', '#');
            addListItem(actionList, 'Show process history', '#');
            addListItem(actionList, 'Cancel process instance', '#');

            return actionList;
        }


        function addListItem(list, title, href) {
            var a = document.createElement('a');
            $(a).attr('href', href);
            $(a).attr('onClick', cancel());
            //onclick="MyFunction();return false;
            a.textContent = title;
            var li = document.createElement('li');
            li.appendChild(a);
            list.appendChild(li);
        }

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

        function cancel() {
            var userIsSure = confirm("are you sure? this cannot be undone!");
            if (userIsSure) {
                //post contorller method!
                //refresh view!
                dataTable.reloadTable(dispatcherPortlet);
                alert('done!');
            }
        }
    //]]>
</script>