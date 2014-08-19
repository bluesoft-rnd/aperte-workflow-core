<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<script type="text/javascript">
	var dispatcherPortlet = '<portlet:resourceURL id="dispatcher"/>';

    var portletNamespace = '&<portlet:namespace/>';

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

</script>
