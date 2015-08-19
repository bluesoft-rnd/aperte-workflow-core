<#import "/spring.ftl" as spring />

<#assign portlet=JspTaglibs["http://java.sun.com/portlet_2_0"] />
<#assign c=JspTaglibs["http://java.sun.com/jsp/jstl/core"]/>

<style>
	#reportLoadingScreen
	{
	    width: 100%;
        position: fixed;
        height: 200px;
        z-index: 1000000;
        float: left;
        bottom: 20%;
        text-align: center;
        font-size: 24px !important;
        background-image: url(<@c.url value='/images/progress_bar/loading_animation_1.gif'/>);
        background-position: 50% 50%;
        background-repeat: no-repeat;
	}
</style>

<script type="text/javascript">
	var dispatcherPortlet = '<@portlet.resourceURL id="dispatcher"/>';
</script>

<div class="apw main-view" style="margin-top:10px;">
	<div class="col-md-10">
		<div class="row">
			<div class="form-group col-md-4">
				<label for="reportTemplates"><@spring.message code="ir.reportTemplateSelect" /></label>
				<select id="reportTemplates" class="aperte-select" style="width:500px;">
					<option value="0"><@spring.message code="ir.selectTemplate" /></option>
				</select>
			</div>
		</div>
	</div>
	<div id="reportParamPanel"></div>
	<div id="reportErrorPanel" class="col-md-12"></div>
	<div id="reportBodyPanel"></div>
	<div id="reportLoadingScreen" hidden><@spring.message code="ir.pleaseWait" /></div>
</div>


<script type="text/javascript">
//<![CDATA[
var interactiveReports = {
	loadReportList: function() {
		$.getJSON(dispatcherPortlet, {
			'controller': 'interactivereportscontroller',
			'action': 'getAvailableReports',
		})
		.done(function(data) {
			$.each(data.data, function (i, item) {
				$('#reportTemplates').append($('<option>', {
					value: item.key,
					text : item.name
				}));
				$('#reportTemplates').select2();
			});
		})
		.fail(function(data, textStatus, errorThrown) {
			console.log('fail', data, textStatus, errorThrown);
		});
	},

	validateParams: function()
	{
	    return [];
	},

	renderParamPanel: function() {
		this.reportKey = null;
		this.reportParams = [];

		$('#reportParamPanel').empty();
		$('#reportBodyPanel').empty();

		var selectedTemplate = $('#reportTemplates').val();

		if (selectedTemplate && selectedTemplate != '0') {
			this.reportKey = selectedTemplate;

	    	$.getJSON(dispatcherPortlet, {
				controller: 'interactivereportscontroller',
				action: 'renderReportParams',
				reportTemplate: selectedTemplate
			})
			.done(function(data) {
				$('#reportParamPanel').html(data.data);
			})
			.fail(function(data, textStatus, errorThrown) {
				console.log('fail', data, textStatus, errorThrown);
			});
		}
	},

	reportKey: null,
	reportParams: [],

	addParam: function(name, paramHandler) {
		this.reportParams.push({ name: name, handler: paramHandler });
	},

	generateReport: function() {
		var params = [];

        var reportErrorPanel = $('#reportErrorPanel');
		reportErrorPanel.empty();

		var errors = this.validateParams();


        $.each(errors, function(index, errorMessage) {
            reportErrorPanel.append('<div class="alert alert-danger" role="alert">' + errorMessage + '</li>');
        });

        $('#reportBodyPanel').empty();

        if(errors.length > 0) { return; }

        $('#reportLoadingScreen').show();

		for (var i = 0; i < this.reportParams.length; ++i) {
			this.reportParams[i].handler(params);
		}

	    $.getJSON(dispatcherPortlet, {
			controller: 'interactivereportscontroller',
			action: 'generateReport',
			reportTemplate: this.reportKey,
			reportParams: JSON.stringify(params)
		})
		.done(function(data) {
			$('#reportLoadingScreen').hide();
			$('#reportBodyPanel').html(data.data);
			if (data.errors && data.errors.length > 0) {
				var html = '<div class="col-md-10"><div class="row"><div class="alert alert-danger" role="alert">';
				$.each (data.errors, function(idx, error) {
					html += (error.source||'') + ' ' + (error.message||'') + '<br/>';
				});
				html += '</div></div></div>';
				$('#reportBodyPanel').html(html);
			}
		})
		.fail(function(data, textStatus, errorThrown) {
			$('#reportLoadingScreen').hide();
			console.log('fail', data, textStatus, errorThrown);
		});
	},

	downloadFile: function(attachmentId) {
	    var urlDownloadFile = this.getNoReplyDispatcherPortlet('casefilescontroller', 'downloadFile');

		this.downloadURL(urlDownloadFile + '&processInstanceId=0&filesRepositoryItemId=' + attachmentId);
	},

	exportReport: function(e, desiredFormat) {
		e.preventDefault();

		var params = [];

		for (var i = 0; i < this.reportParams.length; ++i) {
			this.reportParams[i].handler(params);
		}

		var urlDownloadFile = this.getNoReplyDispatcherPortlet('interactivereportscontroller', 'exportReport');

		this.downloadURL(urlDownloadFile + '&reportTemplate=' + this.reportKey +
					'&reportParams=' + encodeURIComponent(JSON.stringify(params)) +
					'&desiredFormat=' + desiredFormat);
	},

	getNoReplyDispatcherPortlet: function(controller, action) {
		var noReplyDispatcherPortlet = dispatcherPortlet.replace('=dispatcher&','=noReplyDispatcher&');
		var url = noReplyDispatcherPortlet + '&controller=' + controller + '&action=' + action;
		return url;
	},

	downloadURL: function(url) {
		var hiddenIFrameID = 'hiddenDownloader';
		var iframe = document.getElementById(hiddenIFrameID);

		if (iframe === null) {
			iframe = document.createElement('iframe');
			iframe.id = hiddenIFrameID;
			iframe.style.display = 'none';
			document.body.appendChild(iframe);
		}
		iframe.src = url;
	},

	toggle: function(elementId, toggleElementId, tdClassToRowSpan) {
		$('#' + elementId).toggle();
		if (toggleElementId) {
			$('#' + toggleElementId)
				.toggleClass('glyphicon-chevron-right')
				.toggleClass('glyphicon-chevron-down');
		}

		if(tdClassToRowSpan)
		{
		    $.each($('.' + tdClassToRowSpan), function (index, item) {
                    var originalRowSpan = $(item).prop('originalRowSpan');
                    if(!originalRowSpan || originalRowSpan == '')
                    {
                        $(item).prop('originalRowSpan', $(item).attr('rowspan'));
                        $(item).attr('rowspan', 1);
                    }
                    else
                    {
                        $(item).removeProp('originalRowSpan');
                        $(item).attr('rowspan', originalRowSpan);

                    }
		    });

		}
	},
	toggleRows: function(elementId, toggleElementId, tdClassToRowSpan)
	{
	        $.each($('.' + elementId), function (index, item) {
	            $(item).toggle();
	        });

    		if (toggleElementId) {
    			$('#' + toggleElementId)
    				.toggleClass('glyphicon-chevron-right')
    				.toggleClass('glyphicon-chevron-down');
    		}

    		if(tdClassToRowSpan)
    		{
    		    $.each($('.' + tdClassToRowSpan), function (index, item) {
                        var originalRowSpan = $(item).attr('originalRowSpan');
                        if(!originalRowSpan || originalRowSpan == '')
                        {
                            $(item).attr('originalRowSpan', $(item).attr('rowspan'));
                            $(item).attr('rowspan', 1);
                        }
                        else
                        {
                            $(item).removeAttr('originalRowSpan');
                            $(item).attr('rowspan', originalRowSpan);

                        }
    		    });

    		}
    	}


};

$(document).ready(function() {
	interactiveReports.loadReportList();

	$('#reportTemplates').change(function() {
		interactiveReports.renderParamPanel();
	});
});
//]]>
</script>