<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="left-menu">
	<div class="start-process-button" id="process-start-button">
		<spring:message code="processes.start.new.process" />
	</div>
	<div class="queues-list" id="queue-view-block">
		<c:forEach var="userQueue" items="${queuesSize}">
				<c:if test="${userQueue.userLogin==aperteUser.login}">
					Tu jest kolejka zalogowanego użytkownika: ${userQueue.userLogin}
					 
				</c:if> 
				<c:if test="${userQueue.userLogin!=aperteUser.login}">
					 Tu jest kolejka użytkownika:   ${userQueue.userLogin}
				</c:if> 
				
				<c:forEach var="queue" items="${userQueue.processesList}">
					<div class="queue-list-row-process">
						<div id="${queue.queueId}" class="queue-list-name"><a class="queue-list-link" onclick="reloadQueue('${queue.queueName}', 'process') "><spring:message code="${queue.queueDesc}" /></a></div>
						<div class="queue-list-size">${queue.queueSize}</div>
						<br style="clear: left;" />
					</div>
				</c:forEach>

				<c:forEach var="queue" items="${userQueue.queuesList}">
					<div class="queue-list-row-queue">
						<div id="${queue.queueId}" class="queue-list-name"><a class="queue-list-link" onclick="reloadQueue('${queue.queueName}', 'queue') "><spring:message code="${queue.queueDesc}" /></a></div>
						<div class="queue-list-size">${queue.queueSize}</div>
						<br style="clear: left;" />
					</div>
				</c:forEach>
		 </c:forEach>
	</div>
</div>
 <script type="text/javascript">
 
	$(document).ready(function()
	{
		$('#new-process-view').hide();
	});
 
	$("#process-start-button").click(
	  function () 
	  {
		showNewProcessPanel();
	  }
	);
 
 </script>
