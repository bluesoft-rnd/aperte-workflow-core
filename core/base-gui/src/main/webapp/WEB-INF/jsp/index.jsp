<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<?xml version="1.0" encoding="UTF-8"?>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=9; IE=8;" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=yes"/>
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/custom.css' />" />
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/bootstrap.css' />" />
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/bootstrap.min.css' />" />
		<script type="text/javascript" src="<spring:url value='js/jquery-1.9.1.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery-plugins.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery-plugins.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.tools.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jqModal.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/bootstrap.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.validate.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/funkcje.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/refresher.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.dataTables.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.dataTables.bootstrap.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.dateFormat-1.0.js' />"></script>

	</head>
	<body>
		<div class="standalone-version-body">
			<%@include file="main.jsp" %>
		</div>
	</body>
