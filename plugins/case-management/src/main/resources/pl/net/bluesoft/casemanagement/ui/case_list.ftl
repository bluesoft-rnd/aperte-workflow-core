<#import "/spring.ftl" as spring />
<#include "windowmanager.ftl"/>
<#assign portlet=JspTaglibs["http://java.sun.com/portlet_2_0"] />


<script type="text/javascript">
	var dispatcherPortlet = '<@portlet.resourceURL id="dispatcher"/>';
	var portletNamespace = '&<@portlet.namespace/>';
	var dataTableLanguage =
    {
        "sInfo": "Wyniki od _START_ do _END_ z _TOTAL_",
        "sEmptyTable": "<@spring.message code='datatable.empty' />",
        "sInfoEmpty": "<@spring.message code='datatable.empty' />",
        "sProcessing": "<@spring.message code='datatable.processing' />",
        "sLengthMenu": "<@spring.message code='datatable.records' />",
        "sInfoFiltered": "",
        "oPaginate": {
            "sFirst": "<@spring.message code='datatable.paginate.firstpage' />",
            "sNext": "<@spring.message code='datatable.paginate.next' />",
            "sPrevious": "<@spring.message code='datatable.paginate.previous' />"
        }

    };
</script>

<div class="modal fade aperte-modal" id="commentModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button id="button-close-comment-modal" type="button" class="close" data-dismiss="modal" onClick="cancelCommentModal()" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel"><@spring.message code="processes.action.button.comment.title" /></h4>
            </div>
            <div class="modal-body">
                <div class="modal-errors"></div>
                <@spring.message code="processes.action.button.comment.body" />

                <textarea id="action-comment-textarea" class="modal-comment-textarea" onkeyup="checkActionCommentValue(event)" ></textarea>
            </div>
            <div class="modal-footer">
                <button id="button-close-comment-modal-2" type="button" class="btn btn-default"  data-dismiss="modal"><@spring.message code="processes.action.button.comment.close" /></button>
                <button id="action-comment-button" type="button" class="btn btn-primary" disabled="true"><@spring.message code="processes.action.button.comment.perform" /></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div>

<div class="modal fade aperte-modal" id="changeOwnerModal" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" id="button-close-changeowner-modal" class="close" data-dismiss="modal" onClick="cancelChangerOwnerModal()" aria-hidden="true">&times;</button>
                <h4 class="modal-title" id="myModalLabel"><@spring.message code="processes.action.button.change.owner.title" /></h4>
            </div>
            <div class="modal-body">
                <div class="modal-owner-errors"></div>

                <div class="form-group input-group-sm ">
                    <label class="col-sm-4 control-label required" name="tooltip" title='<@spring.message code="processes.action.button.change.owner.label.tootip" />' for="change-owner-select"><@spring.message code="processes.action.button.change.owner.label" /></label>
                    <div id="change-owner-select" class="col-sm-8" data-placeholder="<@spring.message code='processes.action.button.change.owner.select.placeholder' />" ></div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" id="button-close-changeowner-modal-2" class="btn btn-default"  data-dismiss="modal"><@spring.message code="processes.action.button.comment.close" /></button>
                <button id="action-changeowner-button"  type="button" class="btn btn-primary" disabled="true"><@spring.message code="processes.action.button.comment.perform" /></button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div>

<div class="modal fade aperte-modal" id="alertModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">


        <div class="panel panel-warning">
            <div class="panel-heading"><h4><@spring.message code="processes.alerts.modal.title" /></h4></div>
            <ul id="alerts-list">

            </ul>
            <button type="button" class="btn btn-warning" data-dismiss="modal" style="margin: 20px;"><@spring.message code="processes.alerts.modal.close" /></button>
        </div>



    </div><!-- /.modal-dialog -->
</div>

<div class="apw main-view">
    <div class="process-tasks-view" id="case-list-view" hidden>
        <table id="caseManagementTable" class="process-table table table-striped" border="1">
            <thead>
            <th style="width:10%;">
                <@spring.message "admin.case.management.results.table.number"/>
            </th>
            <th style="width:10%;">
                <@spring.message "admin.case.management.results.table.definitionName"/>
            </th>
            <th style="width:15%;">
                <@spring.message "admin.case.management.results.table.name"/>
            </th>
            <th style="width:20%;">
                <@spring.message "admin.case.management.results.table.currentStageName"/>
            </th>
            <th style="width:15%;">
                <@spring.message "admin.case.management.results.table.actions"/>
            </th>
            <th style="width:15%;">
                <@spring.message "admin.case.management.results.table.createDate"/>
            </th>
            <th style="width:15%;">
                <@spring.message "admin.case.management.results.table.modificationDate"/>
            </th>
            </thead>
            <tbody></tbody>
        </table>
    </div>

    <div id="case-data-view" class="process-data-view" hidden="false">
        <div id="case-vaadin-widgets" class="vaadin-widgets-view">
        </div>

        <div id="case-actions-list" class="actions-view">
        </div>
    </div>

    <div id="process-data-view" class="process-data-view marginesy" hidden="true">
            <div id="vaadin-widgets" class="vaadin-widgets-view">
            </div>
            <div id="actions-list" class="actions-view">
            </div>
        </div>

    <div class="modal fade aperte-modal" id="alertModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="panel panel-warning">
                <div class="panel-heading"><h4><@spring.message code="processes.alerts.modal.title" /></h4></div>
                <ul id="case-alerts-list">

                </ul>
                <button type="button" class="btn btn-warning" data-dismiss="modal" style="margin: 20px;">
                    <@spring.message code="processes.alerts.modal.close" />
                </button>
            </div>
        </div>
        <!-- /.modal-dialog -->
    </div>

</div>

<script type="text/javascript">
//<![CDATA[
    var caseManagement = {}
    caseManagement['tableInitialized'] = false;
    caseManagement['alertsShown'] = false;
    caseManagement['alertsInit'] = false;
    var tempButton;
    var tempActionName;
    var tempSkipSaving;
    var tempTaskId;
    var tempCommentNeeded;
    var tempChangeOwner;
    var tempChangeOwnerAttrKey;
    var alertsShown = false;
    var alertsInit = false;

  	$(document).ready(function()
  	{
  	    windowManager.addView("loading-screen");
        windowManager.addView("process-data-view");
        windowManager.addView("process-panel-view");

        window.scrollTo(0, 0);
        <#if caseId?has_content>
            caseManagement.loadCaseView('${caseId?string}');
        <#else>
            caseManagement.showCaseList();
        </#if>
    });

    caseManagement.initDataTable = function()
    {
        if(caseManagement.tableInitialized == true)
            return;

        caseManagement.caseListDT = new AperteDataTable("caseManagementTable",
            [
                 { "sName":"number", "bSortable": true ,"mData": function(object) { return caseManagement.generateNameColumn(object) }
                 },
                 { "sName":"definitionName", "bSortable": false ,"mData": "definitionName"},
                 { "sName":"name", "bSortable": true , "mData": "name"},
                 { "sName":"currentStageName", "bSortable": false ,"mData": "currentStageName"},
                 { "sName": "actions", "bSortable": false, "mData": function(object) {return caseManagement.getActionButtons(object) }
                 },
                 { "sName":"createDate", "bSortable": true ,"mData": "createDate"},
                 { "sName":"modificationDate", "bSortable": true ,"mData": "modificationDate"}
            ],
            [[ 5, "desc" ]]
        );

        caseManagement.caseListDT.addParameter("controller", "casemanagementcontroller");
        caseManagement.caseListDT.addParameter("action", "getAllCasesPaged");
        // if (window.console) console.log(dispatcherPortlet);
        caseManagement.caseListDT.reloadTable(dispatcherPortlet);
        caseManagement.tableInitialized = true;
    }

	caseManagement.generateNameColumn = function(caseInstance) {
        // if (window.console) console.log(caseInstance);
        var showOnClickCode = 'onclick="caseManagement.loadCaseView(' + caseInstance.id + ')"';
        // if (window.console) console.log(showOnClickCode);
        return '<a class="process-view-link"  '+ showOnClickCode + ' >' + caseInstance.number + '</a>';
    }

   caseManagement.getActionButtons = function(caseInstance) {
        var processesList = JSON.parse(caseInstance.caseStateProcessesJson);
        var html = '';
        if(processesList != null) {
            html += '<div class="btn-group" role="group">';
            $.each(processesList, function(index, item) {
                html += '<button type="button" id="shortcut-action-button-' + item.bpmDefinitionKey + '" class="btn btn-'+ item.processActionType + '" title="' + item.processLabel + '" onclick="caseManagement.startProcess(\'' +caseInstance.id+ '\',\'' + item.bpmDefinitionKey + '\')">'
                html+='<span class="glyphicon glyphicon-' + item.processIcon +'" aria-hidden="true"></span>'
                html+='</button>';
            });
            html += '</div>';
        }

        return html;
   }

    caseManagement.loadCaseView = function(caseId)
    {
        caseManagement.changeUrl('?caseId=' + caseId);
        //windowManager.showLoadingScreen();

        var widgetJson = $.getJSON(dispatcherPortlet, {
                "controller": "casemanagementcontroller",
                "action": "loadCase",
                "caseId" : caseId
            })
            .done(function(data) {
                // if (window.console) console.log(data);
                caseManagement.clearAlerts();
                // windowManager.showProcessData();
                caseManagement.hideCaseList();
                caseManagement.showCaseData();
                $('#case-data-view').empty();
                $("#case-data-view").append(data.data);
                caseManagement.enableButtons();
                // checkIfViewIsLoaded();
            })
            .fail(function(data, textStatus, errorThrown) {
            }
        );
    }

    caseManagement.showCaseData = function() {
        $('#case-data-view').fadeIn(500);
    }

    caseManagement.hideCaseData = function() {
        $('#case-data-view').hide();
    }

    caseManagement.hideCaseList = function() {
        $('#case-list-view').hide();
    }

    caseManagement.showCaseList = function()
    {
        caseManagement.initDataTable();
        $('#case-list-view').fadeIn(500);
    }

    caseManagement.onCloseButton = function() {
        caseManagement.hideCaseData();
        caseManagement.clearCaseView();
        caseManagement.showCaseList();
    }

    caseManagement.clearCaseView = function() {
        widgets = []
    }

    caseManagement.enableButtons = function() {
   		$('#case-actions-list').find('button').prop('disabled', false);
   	}

   	caseManagement.disableButtons = function() {
		$('#case-actions-list').find('button').prop('disabled', true);
	}

    caseManagement.changeUrl = function(newUrl) {
        if(window.history && typeof(window.history.pushState) === 'function')
        {
            if(newUrl == '')
            {
                var currentUrl = location.href.replace(/&?taskId=([^&]$|[^&]*)/i, "");
                window.history.pushState('', '', currentUrl);
            }
            else
            {
                window.history.pushState('', '', newUrl);
            }
        }
    }

    caseManagement.onSaveButton = function(caseId) {
        caseManagement.disableButtons();
        caseManagement.saveAction(caseId);
    }

    caseManagement.saveAction = function(caseId) {
		caseManagement.clearAlerts();

		var errors = [];
		<!-- Validate html widgets -->
		$.each(widgets, function() {
			var errorMessages = this.validate();
			if(!errorMessages) {
			} else {
				$.each(errorMessages, function() {
				    errors.push(this);
					caseManagement.addAlert(this);
				});
			}
	    });

		if(errors.length > 0) {
			caseManagement.enableButtons();
			return;
		}

		var widgetData = [];

		$.each(widgets, function() {
			var widgetDataBean = new WidgetDataBean(this.widgetId, this.name, this.getData());
			widgetData.push(widgetDataBean);
	    });

		var JsonWidgetData = JSON.stringify(widgetData, null, 2);

		var state = 'OK';
		var newBpmTask = $.getJSON(dispatcherPortlet,
		{
		    "controller": "casemanagementcontroller",
		    "action": "saveAction",
			"caseId": caseId,
			"widgetData": JsonWidgetData
		})
		.done(function(data) {
			if(data.errors != null) {
				caseManagement.addAlerts(data.errors);
			}
		})
		.always(function() {
			caseManagement.enableButtons();
		})
		.fail(function(data) {
			caseManagement.addAlerts(data.errors);
		});

		return state;
	}

	caseManagement.addAlert = function(alertMessage) {
		if(caseManagement.alertsShown == false) {
			if(caseManagement.alertsInit == false) {
				caseManagement.alertsInit = true;
				$('#alertModal').appendTo("body").modal({
				    keyboard: false
				});
				$('#alertModal').on('hidden.bs.modal', function (e) {
					caseManagement.clearAlerts();
					caseManagement.alertsShown = false;
				});

			} else {
				$('#alertModal').appendTo("body").modal('show');
			}
			caseManagement.alertsShown = true;
		}
		$('#case-alerts-list').append('<li><h5>'+alertMessage+'</h5></li>');
	}

	caseManagement.clearAlerts = function() {
		$('#case-alerts-list').empty();
	}

	caseManagement.addAlerts = function(alertsMessages) {
		caseManagement.clearAlerts();
		$.each(alertsMessages, function() {
			caseManagement.addAlert(this.message);
		});
	}

    caseManagement.startProcess = function(caseId, bpmDefinitionKey) {
    		windowManager.showLoadingScreen();
    		caseManagement.hideCaseList();

    		var widgetJson = $.getJSON(dispatcherPortlet, {
    		    "controller": "casemanagementcontroller",
    		    "action": "startProcessInstance",
    		    "caseId": caseId,
    			"bpmDefinitionKey": bpmDefinitionKey,
    			"nocache": new Date().getTime()
    		})
    		.done(function(data)
    		{
    			<!-- Errors handling -->
    			windowManager.clearErrors();
    			caseManagement.clearAlerts();

    			var errors = [];
    			$.each(data.errors, function() {
    				errors.push(this);
    				windowManager.addError(this.message);
    			});

    			if(errors.length > 0) { return; }

    			if (data.taskId!=null) {
    			    caseManagement.hideCaseList();
    				caseManagement.loadProcessView(data.taskId);
    				windowManager.changeUrl('?taskId=' + data.taskId);
    			} else {
    			    caseManagement.caseListDT.reloadTable(dispatcherPortlet);
    				caseManagement.showCaseList();
    				caseManagement.hideCaseData();
    			}
    		});
    }


     caseManagement.loadProcessView = function(taskId) {
            windowManager.changeUrl('?taskId='+taskId);
            windowManager.showLoadingScreen();

            var widgetJson = $.get('<@portlet.resourceURL id="loadTask"/>', {
                "taskId": taskId,
                "nocache": new Date().getTime()
            })
            .done(function(data) {
                caseManagement.clearAlerts();
                caseManagement.hideCaseData();
                windowManager.showProcessData();
                $('#process-data-view').empty();
                $('#case-data-view').empty();
                $("#process-data-view").append(data);
                checkIfViewIsLoaded();
            })
            .fail(function(data, textStatus, errorThrown) {
            });
        }

        function checkIfViewIsLoaded() {
            if(vaadinWidgetsCount == vaadinWidgetsLoadedCount) {
                caseManagement.enableButtons();
                enableButtons();
            }
        }

        function enableButtons(){
            $('#actions-list').find('button').prop('disabled', false);
        }

        function disableButtons(){
            $('#actions-list').find('button').prop('disabled', true);
        }

    /* Check for comment required field */
	function performAction(button, actionName, skipSaving, commentNeeded, changeOwner, changeOwnerAttributeKey, taskId)
	{
		if(skipSaving != true)
		{
			clearAlerts();

			var errors = fullValidate(actionName);

			if(errors.length > 0)
			{
				enableButtons();
				return;
			}
		}
		if(commentNeeded == true)
		{

			tempButton = button;
			tempActionName = actionName;
			tempSkipSaving = skipSaving;
			tempTaskId = taskId;
			tempCommentNeeded = commentNeeded;


			$('#action-comment-textarea').val('');
			$('#commentModal').appendTo("body").modal({
			  keyboard: false
			});
			$('#commentModal').on('hidden.bs.modal', function (e) {
			  	$('#modal-errors').empty();
				$('#action-comment-textarea').val('');
				enableButtons();
			});

		}
		else if(changeOwner == true)
		{
			tempButton = button;
			tempActionName = actionName;
			tempSkipSaving = skipSaving;
			tempTaskId = taskId;
			tempCommentNeeded = commentNeeded;
			tempChangeOwner = changeOwner;
			tempChangeOwnerAttrKey = changeOwnerAttributeKey;


			$('#change-owner-select').val('');
			$('#changeOwnerModal').appendTo("body").modal({
			  keyboard: false
			});
			$('#changeOwnerModal').on('hidden.bs.modal', function (e) {
			  	$('#modal-errors').empty();
				$('#change-owner-select').val('');
				enableButtons();
			});

			$("#change-owner-select").select2({
				minimumInputLength: 3,
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
						  $.each(data.data, function(index, item){
							results.push({
							  id: item.login,
							  text: getReceivingPersonCaption(item)
							});
						  });
						  return {
							  results: results
						  };
					}
				}
			});
			$("#change-owner-select").on("change", function(e)
			{
				var selectedLogin = $('#change-owner-select').val();
				$('#action-changeowner-button').attr("disabled", (!selectedLogin || selectedLogin == ''));
			});
		}
		else
		{
			performActionWithoutComment(button, actionName, skipSaving, taskId, false, '', false, '', '');
		}

	}


	function performActionWithoutComment(button, actionName, skipSaving, taskId, commentNeeded, comment, changeOwner, changeOwnerAttributeKey, changeOwnerAttributeValue)
	{
		var widgetData = [];

		if(skipSaving != true)
		{
			clearAlerts();

			var errors = [];

			var validateAllEnabled = true;

			$.each(widgets, function() {
				if (this.isValidateAllEnabled && !this.isValidateAllEnabled(actionName)) {
					validateAllEnabled = false;
				}
			});

			/* Validate html widgets */
			$.each(widgets, function()
			{
				var errorMessages = validateAllEnabled ? this.validate(actionName) :
								this.partialValidate ? this.partialValidate(actionName) : [];

				$.each(errorMessages, function() {
					errors.push(this);
					addAlert(this);
				});
			});

			if(errors.length > 0)
			{
				enableButtons();
				return;
			}

			$.each(widgets, function()
			{
				var widgetDataBean = new WidgetDataBean(this.widgetId, this.name, this.getData());
				widgetData.push(widgetDataBean);
			});
		}

		var performActionArgs =
		{
			button: button,
			actionName: actionName,
			skipSaving: skipSaving,
			taskId: taskId,
			commentNeeded: commentNeeded,
			comment: comment,
			changeOwner: changeOwner,
			changeOwnerAttributeKey: changeOwnerAttributeKey,
			changeOwnerAttributeValue: changeOwnerAttributeValue,
			widgetData: widgetData
		};

        for (var i = 0; i < widgets.length; ++i)
        {
        	var widget = widgets[i];

        	if (widget.beforePerformAction)
        	{
        		var handled = widget.beforePerformAction(performActionRequest, performActionArgs);

        		if (handled)
        		{
        			return;
        		}
        	}
        }

        // no widget intercepted the action -> default handling

		performActionRequest(performActionArgs);
	}

	function performActionRequest(args)
	{
		var JsonWidgetData = "[{}]";

		if (args.widgetData.length > 0)
		{
			JsonWidgetData = JSON.stringify(args.widgetData, null, 2);
		}

        var newBpmTask = $.post('<@portlet.resourceURL id="performAction"/>',
        {
			"taskId": args.taskId,
			"actionName": args.actionName,
			"skipSaving": args.skipSaving,
			"commentNeeded": args.commentNeeded,
			"comment": args.comment,
			"changeOwner": args.changeOwner,
			"changeOwnerAttributeKey": args.changeOwnerAttributeKey,
			"changeOwnerAttributeValue": args.changeOwnerAttributeValue,
			"widgetData": JsonWidgetData
        }, null, 'json')
        .done(function(data)
        {
			/* Errors handling */
			windowManager.clearErrors();

			var errors = [];
			$.each(data.errors, function() {
				errors.push(this);
				addAlert(this.message);
			});

			if(errors.length > 0) { return; }

			// reloadQueues();

			if(!data)
			{
				closeProcessView();
				windowManager.showProcessList();

				return;
			}
			else if(data.errors.length > 0)
			{
				addAlerts(data.errors);
				return;
			}
			else if(!data.nextTask)
			{
				closeProcessView();
				windowManager.showProcessList();

				return;
			}

			var taskId = data.nextTask.taskId;
			if(taskId)
			{
				loadProcessView(taskId);
			}
			else
			{
				closeProcessView();
				windowManager.showProcessList();
			}
        })
        .fail(function(XMLHttpRequest, textStatus, errorThrown) { addAlert(errorThrown); })
        .always(function(data)
        {
            if(data != null)
            {
                enableButtons();
            }
            tempChangeOwner = null;
            tempChangeOwnerAttrKey = null;
            tempCommentNeeded = null;
        });

	}

	function addAlert(alertMessage)
    {
        if(alertsShown == false)
        {
            if(alertsInit == false)
            {
                alertsInit = true;
                $('#alertModal').appendTo("body").modal({
                  keyboard: false
                });
                $('#alertModal').on('hidden.bs.modal', function (e) {
                    clearAlerts();
                    alertsShown = false;
                });

            }
            else
            {
                $('#alertModal').appendTo("body").modal('show');
            }
            alertsShown = true;
        }

        $('#alerts-list').append('<li><h5>'+alertMessage+'</h5></li>');

    }

	function clearAlerts(){
        $('#alerts-list').empty();
    }

    function checkActionCommentValue(event)
    {
        var comment = $('#action-comment-textarea').val();

         $('#action-comment-button').attr("disabled", comment == '');
    }

    function cancelCommentModal()
    {
        $('#commentModal').modal('hide');

    }

    $(document).ready(function () {
    		$("#button-close-changeowner-modal").on("click", function () {
    			cancelChangerOwnerModal();
    		});
    		$("#button-close-changeowner-modal-2").on("click", function () {
    			cancelChangerOwnerModal();
    		});
    		$("#action-changeowner-button").on("click", function () {
    			performChangeOwnerModal();
    		});

    		$("#button-close-comment-modal").on("click", function () {
    			cancelCommentModal();
    		});
    		$("#button-close-comment-modal-2").on("click", function () {
    			cancelCommentModal();
    		});
    		$("#action-comment-button").on("click", function () {
    			performCommentModal();
    		});
    	});

    	function cancelCommentModal()
    	{
    		$('#commentModal').modal('hide');

    	}

    	function cancelChangerOwnerModal()
    	{
    		$('#changeOwnerModal').modal('hide');
    	}

    	function performCommentModal()
    	{
    		var comment = $('#action-comment-textarea').val();
    		$('#commentModal').modal('hide');
    		if(!comment)
    		{
    			$('#modal-errors').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button><@spring.message code="processes.action.button.comment.empty" /></div>');
    			return;
    		}

    		performActionWithoutComment(tempButton, tempActionName, tempSkipSaving, tempTaskId, tempCommentNeeded, comment, tempChangeOwner, '', '');
    	}

    	function performChangeOwnerModal()
    	{
    		var newOwnerLogin = $('#change-owner-select').val();
    		$('#changeOwnerModal').modal('hide');
    		if(!newOwnerLogin)
    		{
    			$('#modal-owner-errors').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button><@spring.message code="processes.action.button.change.owner.select.empty" /></div>');
    			return;
    		}

    		performActionWithoutComment(tempButton, tempActionName, tempSkipSaving, tempTaskId, false, '',  true, tempChangeOwnerAttrKey, newOwnerLogin);
    	}

    function closeProcessView()
    {

        $('#process-data-view').empty();
        $('#actions-list').empty();
        windowManager.showLoadingScreen();
        caseManagement.caseListDT.reloadTable(dispatcherPortlet);
        caseManagement.hideCaseData();
        caseManagement.showCaseList();
    }


//]]>

</script>

