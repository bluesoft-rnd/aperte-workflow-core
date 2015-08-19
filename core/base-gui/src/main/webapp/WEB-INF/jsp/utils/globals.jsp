<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<script type="text/javascript">
	var dispatcherPortlet = '<portlet:resourceURL id="dispatcher"/>';

	var queueListPortlet = '<portlet:resourceURL id="loadQueue"/>';
    var processListPortlet = '<portlet:resourceURL id="loadProcessesList"/>';
    var claimTaskFromQueuePortlet = '<portlet:resourceURL id="claimTaskFromQueue"/>';
    var portletNamespace = '&<portlet:namespace/>';
    var iconsBaseUrl = '<c:url value="/images/documents_icons"/>';

    var dataTableLanguage =
    {
        "sInfo": "Wyniki od _START_ do _END_ z _TOTAL_",
        "sEmptyTable": "<spring:message code='datatable.empty' />",
        "sInfoEmpty": "<spring:message code='datatable.empty' />",
        "sProcessing": "<spring:message code='datatable.processing' />",
        "sLengthMenu": "<spring:message code='datatable.records' />",
        "sInfoFiltered": "",
        "oPaginate": {
            "sFirst": "<spring:message code='datatable.paginate.firstpage' />",
            "sNext": "<spring:message code='datatable.paginate.next' />",
            "sPrevious": "<spring:message code='datatable.paginate.previous' />"
        }

    };

    var fileIcons = {
		'default' : 'default.png',
		'txt' : 'txt.png',
		'pdf' : 'pdf.png',
		'bmp' : 'bmp.png',
		'gif' : 'gif.png',
		'jpg' : 'jpg.png',
		'jpeg' : 'jpg.png',
		'ods' : 'ods.png',
		'odt' : 'odt.png',
		'zip' : 'zip.png',
		'rar' : 'rar.png',
		'7z' : '7zip.png',
		'msg' : 'msg.png',
		'png' : 'png.png',
		'doc' : 'doc.png',
		'docx' : 'docx.png',
		'ppt' : 'ppt.png',
		'pptx' : 'ppt.png',
		'xls' : 'xls.png',
		'xlsx' : 'xlsx.png',
		'' : 'default.png'
	};
	function getFileIcon(filename){
		if (typeof fileIcons == 'undefined') {console.log('WARNING! fileIcons undefined'); return;}
		if (typeof fileIcons != 'object') {console.log('WARNING! fileIcons not an object'); return;}
		if (typeof fileIcons['default'] == 'undefined') {console.log('WARNING! fileIcons["default"] undefined');}
		if (typeof filename == 'undefined') return '';
		if (typeof filename != 'string') return '';
		var extDotIndex = filename.lastIndexOf(".");
		if (extDotIndex <= -1){
			return fileIcons['default'];
		}
		var ext = filename.substring(extDotIndex+1).toLowerCase();
		var iconFile = fileIcons[ext];
		if (typeof iconFile == 'undefined'){
			return fileIcons['default'];
		}
		return iconFile;
	}

	function getFileIconUrl(filename){
		if (typeof getFileIcon != 'function'){console.log('WARNING! getFileIcon undefined'); return;}
		var iconFileName = getFileIcon(filename);
		if (!iconFileName){
			return '';
		} else {
			return iconsBaseUrl + '/' + iconFileName;
		}
	}

	function getFileIconHtml(filename){
		if (typeof getFileIcon != 'function'){console.log('WARNING! getFileIconUrl undefined'); return;}
		var iconFileUrl = getFileIconUrl(filename);
		if (!iconFileUrl){
			return '';
		} else {
			return '<img src="'+iconFileUrl+'" style="width: 16px; height: 16px;" />';
		}
	}

</script>
