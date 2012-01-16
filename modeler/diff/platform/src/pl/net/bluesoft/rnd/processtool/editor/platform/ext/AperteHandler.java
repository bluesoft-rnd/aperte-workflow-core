/**
 *
 */
package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import com.signavio.platform.annotations.HandlerConfiguration;
import com.signavio.platform.core.Platform;
import com.signavio.platform.core.PlatformProperties;
import com.signavio.platform.handler.BasisHandler;
import com.signavio.platform.security.business.FsAccessToken;
import com.signavio.platform.security.business.FsSecureBusinessObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


@HandlerConfiguration(uri = "/aperte_definitions", rel="aperte")
public class AperteHandler extends BasisHandler {
	
	private final static Logger logger = Logger.getLogger(AperteHandler.class);
	
	public AperteHandler(ServletContext servletContext) {
		super(servletContext);
		

	}

	/**
	 * Returns a plugins configuration xml file that fits to the current user's license.
	 * @throws Exception
	 */
	@Override
    public <T extends FsSecureBusinessObject> void doGet(HttpServletRequest req, HttpServletResponse res, FsAccessToken token, T sbo) {
  	
  		// Set status code and write the representation
  			res.setStatus(200);
  			res.setContentType("application/json");


        try {
            JSONObject ret = getStencilExtensionString();
            ret.write( res.getWriter() );

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private JSONObject getStencilExtensionString() throws IOException, JSONException {
    	String jo = getDataFromServer();
    	
    	JSONArray pjo = ((jo == null) ? null : new JSONArray(jo));


        JSONObject root = new JSONObject();

        setGlobalParams(root);
        root.put( "stencils", new JSONArray());

        addProperties(pjo, root);

        addRules(root);
        removeStencils(root);

        return root;
    }

    private void addProperties(JSONArray pjo, JSONObject root) throws JSONException {
        JSONArray properties = new JSONArray();
        root.put( "properties", properties);

        modifyTaskProperties(pjo, properties);
        modifySequenceFlowProperties(properties);
        modifyBpmnProperties(properties);
    }

    private void modifyBpmnProperties(JSONArray properties) throws JSONException {
        JSONObject obj1 = new JSONObject();
        properties.put(obj1);

        JSONArray o_roles= new JSONArray();
        o_roles.put("BPMNDiagram");
        obj1.put("roles",o_roles);

        JSONArray o_prop= new JSONArray();
        obj1.put("properties",o_prop);

        o_prop.put(getProcessFileName());
        o_prop.put(getProcessVersion());
        o_prop.put(getBundleName());
        o_prop.put(getBundleDescription());
        o_prop.put(getProcessToolDeployment());
		o_prop.put(getQueueConf());
        o_prop.put(getMessageProperties());
    }

    private JSONObject getMessageProperties() throws JSONException {
      	 JSONObject o = new JSONObject();
           o.put("id","messages-properties");
   		   o.put("type","String");
   		   o.put("title","messages.properties");
   		   o.put("description","messages.properties");
   		   o.put("readonly",true);
   		   o.put("optional",true);
   		   return o;
   		   
      }
  
    private JSONObject getProcessFileName() throws JSONException {
    	 JSONObject o = new JSONObject();
         o.put("id","aperte-process-filename");
		   o.put("type","String");
		   o.put("title","Aperte process filename");
		   o.put("description","Aperte process filename");
		   o.put("readonly",false);
		   o.put("optional",true);
         return o;
    }
    
    
    
    private JSONObject getProcessVersion() throws JSONException {
    	 JSONObject o = new JSONObject();
         o.put("id","aperte-process-version");
		   o.put("type","String");
		   o.put("title","Aperte process version number");
		   o.put("description","Aperte process version number");
		   o.put("readonly",false);
		   o.put("optional",true);
         return o;
    }
    
  
    
    private JSONObject getBundleName() throws JSONException {
 	   JSONObject o = new JSONObject();
        o.put("id","mf-bundle-name");
		   o.put("type","String");
		   o.put("title","Manifest: Bundle-Name");
		   o.put("description","Manifest: Bundle-Name");
		   o.put("readonly",false);
		   o.put("optional",true);
		   
        return o;
    }
    
    private JSONObject getBundleDescription() throws JSONException {
  	   JSONObject o = new JSONObject();
         o.put("id","mf-bundle-description");
 		   o.put("type","String");
 		   o.put("title","Manifest: Bundle-Description");
 		   o.put("description","Manifest: Bundle-Description");
 		   o.put("readonly",false);
 		   o.put("optional",true);
         return o;
     }
    
    private JSONObject getProcessToolDeployment() throws JSONException {
   	   JSONObject o = new JSONObject();
          o.put("id","mf-processtool-deployment");
  		   o.put("type","String");
  		   o.put("title","Manifest: ProcessTool-Process-Deployment");
  		   o.put("description","Manifest: ProcessTool-Process-Deployment");
  		   o.put("readonly",false);
  		   o.put("optional",true);
          return o;
      }
	  
	  

    private void modifyTaskProperties(JSONArray pjo, JSONArray properties) throws JSONException {
        JSONObject obj1 = new JSONObject();
        properties.put(obj1);

        JSONArray o_roles= new JSONArray();
        o_roles.put("Task");
        obj1.put("roles",o_roles);

        JSONArray o_prop= new JSONArray();
        obj1.put("properties",o_prop);

        o_prop.put(getAperteConf());
        
        o_prop.put(getAperteTaskTypes(pjo));
    }
    
    private void modifySequenceFlowProperties(JSONArray properties) throws JSONException {
    	JSONObject obj1 = new JSONObject();
        properties.put(obj1);

        JSONArray o_roles= new JSONArray();
        o_roles.put("SequenceFlow");
        obj1.put("roles",o_roles);

        JSONArray o_prop= new JSONArray();
        obj1.put("properties",o_prop);
        
        JSONObject o = new JSONObject();
        o.put("id","skip-saving");    o.put("type","Boolean");
	    o.put("title","Skip saving"); o.put("description","Skip saving");
	    o.put("readonly",false);  o.put("optional",false);
	    o_prop.put(o);
	    
	    o = new JSONObject();
        o.put("id","auto-hide");    o.put("type","Boolean");
	    o.put("title","Auto hide"); o.put("description","Auto hide");
	    o.put("readonly",false);  o.put("optional",false);
	    o_prop.put(o);
	    
	    o = new JSONObject();
        o.put("id","priority");    o.put("type","Integer");
	    o.put("title","Priority"); o.put("description","Priority");
	    o.put("readonly",false);  o.put("optional",false);
	    o_prop.put(o);
	    
    }

    private void setGlobalParams(JSONObject root) throws JSONException {
        root.put( "title", "Aperte Core Elements");
        root.put( "title_de","Aperte Core Elements");
        root.put( "namespace","http://oryx-editor.org/stencilsets/extensions/bpmn2.0basicsubset#");
        root.put( "description","A basic subset of BPMN 2.0 containing only task, sequence flow, start event, end event, parallel gateway and data-based XOR.");
        root.put( "extends","http://b3mn.org/stencilset/bpmn2.0#");
    }

    private void addRules(JSONObject root) throws JSONException {
        JSONObject rules = new JSONObject();
        root.put(  "rules",rules);
        rules.put("connectionRules", new JSONArray());
        rules.put("cardinalityRules", new JSONArray());
        rules.put("containmentRules", new JSONArray());
    }

    private void removeStencils(JSONObject root) throws JSONException {
        JSONArray removestencils = new JSONArray();
        root.put(  "removestencils",removestencils);
        removestencils.put("ITSystem");
        removestencils.put("EventSubprocess");
        removestencils.put("CollapsedEventSubprocess");
        removestencils.put("Subprocess");
        removestencils.put("DataStore");
        removestencils.put("Message");
        removestencils.put("StartErrorEvent");
        removestencils.put("StartCompensationEvent");
        removestencils.put("StartParallelMultipleEvent");
        removestencils.put("StartEscalationEvent");
        removestencils.put("IntermediateParallelMultipleEventCatching");
        removestencils.put("IntermediateEscalationEvent");
        removestencils.put("EndEscalationEvent");
        removestencils.put("IntermediateEscalationEventThrowing");
        removestencils.put("EventbasedGateway");
        removestencils.put("InclusiveGateway");
        removestencils.put("ComplexGateway");
        removestencils.put("CollapsedPool");
        removestencils.put("processparticipant");
        removestencils.put("Group");
        removestencils.put("TextAnnotation");
        removestencils.put("DataObject");
        removestencils.put("StartConditionalEvent");
        removestencils.put("StartSignalEvent");
        removestencils.put("StartMultipleEvent");
        removestencils.put("IntermediateEvent");
        removestencils.put("IntermediateMessageEventCatching");
        removestencils.put("IntermediateMessageEventThrowing");
        removestencils.put("IntermediateTimerEvent");
        removestencils.put("IntermediateErrorEvent");
        removestencils.put("IntermediateCancelEvent");
        removestencils.put("IntermediateCompensationEventCatching");
        removestencils.put("IntermediateCompensationEventThrowing");
        removestencils.put("IntermediateConditionalEvent");
        removestencils.put("IntermediateSignalEventCatching");
        removestencils.put("IntermediateSignalEventThrowing");
        removestencils.put("IntermediateMultipleEventCatching");
        removestencils.put("IntermediateMultipleEventThrowing");
        removestencils.put("IntermediateLinkEventCatching");
        removestencils.put("IntermediateLinkEventThrowing");
        removestencils.put("EndMessageEvent");
        removestencils.put("EndErrorEvent");
        removestencils.put("EndCancelEvent");
        removestencils.put("EndCompensationEvent");
        removestencils.put("EndSignalEvent");
        removestencils.put("EndMultipleEvent");
        removestencils.put("EndTerminateEvent");
        removestencils.put("Association_Undirected");
        removestencils.put("Association_Unidirectional");
        removestencils.put("Association_Bidirectional");
        removestencils.put("CollapsedSubprocess");
        removestencils.put("StartTimerEvent");
        removestencils.put("MessageFlow");
        removestencils.put("StartMessageEvent");
        root.put("removeproperties",new JSONArray() );
    }

    private  String getDataFromServer() throws IOException {
        PlatformProperties props = Platform.getInstance().getPlatformProperties();
    	String stepListUrl = props.getServerName() + props.getJbpmGuiUrl() + props.getAperteStepListUrl();
        try {
        	URL url = new URL(stepListUrl);
	        URLConnection conn = url.openConnection();
	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        StringBuffer sb = new StringBuffer();
	        String line;
	        while ((line = rd.readLine()) != null)
	        {
	            sb.append(line);
	        }
	        rd.close();
	        return sb.toString();
        } catch (IOException e) {
        	logger.error("Error reading data from " + stepListUrl, e);
        	return null;
        }
    }

    private  JSONObject getAperteConf() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("id","aperte-conf");
		    o.put("type","String");
		    o.put("title","Aperte Configuration");
		    o.put("description","Extended configuration for aperte reports");
		    o.put("readonly",true);
		    o.put("optional",true);
        return o;
    }
	private JSONObject getQueueConf() throws JSONException {
   	   JSONObject o = new JSONObject();
          o.put("id","queue-conf");
  		   o.put("type","String");
  		   o.put("title","Queue configuration");
  		   o.put("description","Queue configuration");
  		   o.put("readonly",true);
  		   o.put("optional",true);
          return o;
      }
    
    
     private  JSONObject getAperteTaskTypes(JSONArray pjo) throws JSONException {
            JSONObject o = new JSONObject();
            o.put("id","tasktype");
		    o.put("type","Choice");
			o.put("title","Tasktype");
			o.put("title_de","Tasktyp");
			o.put("value","None");
			o.put("description","Defines the tasks type which is shown in the left upper corner of the task.");
			o.put("description_de","Definiert den Aufgabentyp, der in der linken oberen Ecke der Task angezeigt wird.");
			o.put("readonly",false);
			o.put("optional",false);
			o.put("refToView","");
            JSONArray items = new JSONArray();
            o.put("items" ,items);

            JSONObject c1 = new JSONObject();
            items.put(c1);
			c1.put("id","c1");
            c1.put("title","None");
			c1.put("title_de","Kein Typ");
			c1.put("value","None");
			c1.put("refToView","none");

            JSONObject c4 = new JSONObject();
            items.put(c4);
            c4.put("id","c4");
			c4.put("title","User");
			c4.put("title_de","Benutzer");
			c4.put("value","User");
		    c4.put("icon" , "activity/list/type.user.png");
			c4.put("refToView","userTask");
			
            if (pjo != null) { 
			 for(int i=0;i<pjo.length();i++){
                JSONObject oo = (JSONObject) pjo.get(i);
                String name = oo.get("name").toString();

                JSONObject cc = new JSONObject();
                items.put(cc);
                cc.put("id","c"+(10+i));
			    cc.put("title",name);
			    cc.put("title_de",name);
			    cc.put("value",name);
		        cc.put("icon" , "activity/list/type.service.png");
			    cc.put("refToView","serviceTask");
             }
            }
        return o;
    }
    
}

