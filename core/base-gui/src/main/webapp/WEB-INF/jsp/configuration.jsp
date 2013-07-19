<%@ page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>



<div id="configuration" hidden="true">
	<div class="toggle-buttons">
	<div class="process-queue-name">
		<spring:message code="configuration.process.table.header" />
	</div>
	<fieldset data-role="controlgroup">
	 <label class="checkbox">
         <input id="process-table-hide-0" type="checkbox"  name="checkme0" onClick="toggleColumn(0);" /><spring:message code="processes.button.hide.processname" />
          </label>
          <label class="checkbox">
          <input id="process-table-hide-1" type="checkbox"  name="checkme1" onClick="toggleColumn(1);" /><spring:message code="processes.button.hide.step" />
           </label>
           <label class="checkbox">
           <input id="process-table-hide-2" type="checkbox"  name="checkme2" onClick="toggleColumn(2);" /><spring:message code="processes.button.hide.processcode" />
            </label>
            <label class="checkbox">
            <input id="process-table-hide-3" type="checkbox"  name="checkme3" onClick="toggleColumn(3);" /> <spring:message code="processes.button.hide.creator" />
             </label>
             <label class="checkbox">
             <input id="process-table-hide-4" type="checkbox"  name="checkme4"  onClick="toggleColumn(4);"/> <spring:message code="processes.button.hide.assignee" />
              </label>
              <label class="checkbox">
              <input id="process-table-hide-5" type="checkbox"  name="checkme5" onClick="toggleColumn(5);" />  <spring:message code="processes.button.hide.creationdate" />
               </label>
               <label class="checkbox">
               <input id="process-table-hide-6" type="checkbox"  name="checkme6"  onClick="toggleColumn(6);" /><spring:message code="processes.button.hide.deadline" />
               </label>
	</fieldset>
	</div>
</div>
<script type="text/javascript">
function toggleColumn(columnNumber)
	{
	if($('input:checkbox[name=checkme'+columnNumber+']').is(':checked')){
	toggleColumnVisible(columnNumber,true);
	}else{
	toggleColumnVisible(columnNumber,false);
	}
		
	}
	
	function selectConfigurationOptions(){
	 $('input:checkbox[name=checkme0]').attr('checked',columnVisibility(0));
 $('input:checkbox[name=checkme1]').attr('checked',columnVisibility(1));
 $('input:checkbox[name=checkme2]').attr('checked',columnVisibility(2));
 $('input:checkbox[name=checkme3]').attr('checked',columnVisibility(3));
 $('input:checkbox[name=checkme4]').attr('checked',columnVisibility(4));
 $('input:checkbox[name=checkme5]').attr('checked',columnVisibility(5));
 $('input:checkbox[name=checkme6]').attr('checked',columnVisibility(6));
	}
</script>


