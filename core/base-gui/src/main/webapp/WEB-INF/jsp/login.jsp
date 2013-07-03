<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="please-log-in-label">
	<div class="process-queue-name">
		<spring:message code="authorization.please.log.in" />
	</div>
	 <c:if test="${isStandAlone==true}">
		<div class="login-form">

				<label class="login-label"><spring:message code='authorization.login.username' /></label>
				<input type="text" id="login-username" class="input-medium login-element-username" >
				<label class="login-label"><spring:message code='authorization.login.password' /></label>
				<input type="password" id="login-password" class="input-medium login-element-password">
			<div id="login-alerts">
			
			</div>

			<button id="login-button-login" type="button" class="btn login-button" onClick="userLogin();" ><spring:message code="authorization.login.button" /></button>
		</div>
	 </c:if> 
</div>

<script type="text/javascript">
	function userLogin()
	{
		$('#login-button-login').prop('disabled', true);
		$('#login-alerts').empty();
		
		$.post('<spring:url value="/user/login.json"/>', 
		{
			"login": $('#login-username').val(),
			"password": $('#login-password').val()
		}, function(data) 
		{
			if(!data)
			{
				console.log( "fail: "+data );
				$('#login-alerts').append('<div class="alert"><button type="button" class="close" data-dismiss="alert">&times;</button><spring:message code="authorization.login.error" /></div>');
				$('#login-button-login').prop('disabled', false);
			}
			else
			{
				console.log( "sucess login: "+data );
				location.reload();
			}
		})

	}
</script>