<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="search-view">
	<label><spring:message code="processes.search.label" /></label>
	<textarea placeholder="<spring:message code='processes.search.textarea.input' />" id="search-expression-text" name="expression-text" minlength="2" required></textarea>
	
	
</div>
 <script type="text/javascript">

	$(document).ready(function()
	{
		$('#search-view').hide();
	});
	
	var delay = (function(){
	  var timer = 0;
	  return function(callback, ms){
		clearTimeout (timer);
		timer = setTimeout(callback, ms);
	  };
	})();
	
	$('#search-expression-text').keyup(function() 
	{
		delay(function(){
		  console.log( "bum text: "+$('#search-expression-text').val()); 
		}, 1000 );
	});
 

 
 </script>
