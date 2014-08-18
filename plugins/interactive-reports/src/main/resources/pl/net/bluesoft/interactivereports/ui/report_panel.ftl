<#import "/spring.ftl" as spring />

<#assign portlet=JspTaglibs["http://java.sun.com/portlet_2_0"] />


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
	<div id="reportBodyPanel"></div>
</div>


<script type="text/javascript">
//<![CDATA[
var interactiveReports = {
	loadReportList: function() {
		$.post(dispatcherPortlet, {
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

	renderParamPanel: function() {
		this.reportKey = null;
		this.reportParams = [];

		$('#reportParamPanel').html('');
		$('#reportBodyPanel').html('');

		var selectedTemplate = $('#reportTemplates').val();

		if (selectedTemplate && selectedTemplate != '0') {
			this.reportKey = selectedTemplate;

	    	$.post(dispatcherPortlet, {
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

		for (var i = 0; i < this.reportParams.length; ++i) {
			this.reportParams[i].handler(params);
		}

	    $.post(dispatcherPortlet, {
			controller: 'interactivereportscontroller',
			action: 'generateReport',
			reportTemplate: this.reportKey,
			reportParams: JSON.stringify(params)
		})
		.done(function(data) {
			$('#reportBodyPanel').html(data.data);
		})
		.fail(function(data, textStatus, errorThrown) {
			console.log('fail', data, textStatus, errorThrown);
		});
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

	toggle: function(elementId, toggleElementId) {
		$('#' + elementId).toggle();
		if (toggleElementId) {
			$('#' + toggleElementId)
				.toggleClass('glyphicon-chevron-right')
				.toggleClass('glyphicon-chevron-down');
		}
	},
};

$(document).ready(function() {
	interactiveReports.loadReportList();

	$('#reportTemplates').change(function() {
		interactiveReports.renderParamPanel();
	});
});
//]]>
</script>