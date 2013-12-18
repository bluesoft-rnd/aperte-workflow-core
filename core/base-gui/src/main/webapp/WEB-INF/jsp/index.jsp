<%@ page import="org.springframework.web.servlet.support.RequestContextUtils" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=9; IE=8;" />
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=yes"/>
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/custom.css' />" />
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/bootstrap.css' />" />
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/bootstrap.min.css' />" />
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/select2.css' />" />
		<link rel="stylesheet" type="text/css" href="<spring:url value='/css/bootstrap-datetimepicker.min.css' />" />
		<script type="text/javascript" src="<spring:url value='js/jquery-1.10.2.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery-plugins.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.cookie.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.tools.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jqModal.js' />"></script> <script type="text/javascript" src="<spring:url value='js/bootstrap.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.validate.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.dataTables.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.timer.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.dataTables.bootstrap.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/jquery.dateFormat-1.0.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/select2.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/bootstrap-datetimepicker.min.js' />"></script>
		<script type="text/javascript" src="<spring:url value='js/ColReorder.js' />"></script>
		<script src="http://maps.google.com/maps/api/js?sensor=true" type="text/javascript"></script>
	</head>
	<body>
		<div class="standalone-version-body">
			<%@include file="main.jsp" %>
		</div>
		

	</body>
