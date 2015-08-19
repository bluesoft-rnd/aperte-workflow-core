<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Application Webapp</title>
    <link href="<c:url value="/css/bootstrap.min.css"/>" rel="stylesheet">
</head>
<body>
<div class="container">
    <div class="jumbotron">
        <h1>Fill application form</h1>
    </div>
    <div class="container">
    <c:if test="${not empty applicationSent}">
    <div style="text-align:center;">
        <c:choose>
            <c:when test= "${applicationSent}">
                <div class="alert alert-success" role="alert">
                    <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                    <span class="sr-only">Info:</span>
                    <b>Thank you for filling out our form. Your application has been sent</b>
                </div>
            </c:when>
            <c:otherwise>
                <div class ="alert alert-danger" role="alert">
                    <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                    <span class="sr-only">Error:</span>
                    <b>An error occured while processing your request. Please try later.</b>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
    </c:if>
    <form:form method="POST" modelAttribute="applicationForm" enctype="multipart/form-data" action="${pageContext.request.contextPath}/apply">
        <div class="form-group">
            <label for="name">Name</label>
            <form:input class="form-control" path="name" />
        </div>
        <div class="form-group">
            <label for="surname">Surname</label>
            <form:input class="form-control" path="surname" />
        </div>
        <div class="form-group">
            <label for="description">Description</label>
            <form:textarea path="description" style="width:100%" rows="5"/>
        </div>
        <div class="form-group">
            <label for="attachmentFile">Attachment</label>
            <form:input path="attachmentFile" type="file"/>
        </div>
        <input type="submit" value="Apply" class="btn btn-primary pull-right" />
    </form:form>
    </div>
</div>
<script src="<c:url value="/js/jquery-1.10.2.min.js"/>"></script>
<script src="<c:url value="/js/bootstrap.min.js"/>"></script>
</body>
</html>