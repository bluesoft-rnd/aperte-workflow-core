<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="please-log-in-label">
	<spring:message code="authorization.please.log.in" />
	
	<div class="login-form">
		<div class="label-container">
			<label class="login-label"><spring:message code='authorization.login.username' /></label>
			<label class="login-label"><spring:message code='authorization.login.password' /></label>
		</div>
		<div class="label-container">
			<input type="text" id="login-username" class="input-medium login-element-username" >
			<input type="password" id="login-password" class="input-medium login-element-password">
		</div>
		<button id="login-button-login" type="button" class="btn login-button" data-toggle="button" onClick="userLogin();" ><spring:message code="authorization.login.button" /></button>
	</div>
	
</div>

<script type="text/javascript">
	function userLogin()
	{
		
	}
</script>