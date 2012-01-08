package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: BLS
 * Date: 18.10.11
 * Time: 15:43
 * To change this template use File | Settings | File Templates.
 */
public class Example {
    public  static void main(String[] args) throws JSONException, IOException {

            String jo = getDataFromServer();
            JSONArray pjo = new JSONArray(getDataFromServer());



            JSONObject root = new JSONObject();

            root.put( "title", "Aperte Core Elements");
            root.put( "title_de","Aperte Core Elements");
            root.put( "namespace","http://oryx-editor.org/stencilsets/extensions/bpmn2.0basicsubset#");
            root.put( "description","A basic subset of BPMN 2.0 containing only task, sequence flow, start event, end event, parallel gateway and data-based XOR.");
            root.put( "extends","http://b3mn.org/stencilset/bpmn2.0#");
            root.put( "stencils", new JSONArray());

            JSONArray properties = new JSONArray();
            root.put( "properties", properties);

            JSONObject obj1 = new JSONObject();
            properties.put(obj1);

            JSONArray o_roles= new JSONArray();
            o_roles.put("Task");
            obj1.put("roles",o_roles);

            JSONArray o_prop= new JSONArray();
            obj1.put("properties",o_prop);

            o_prop.put(getAperteConf());
            o_prop.put(getAperteUrls(pjo)) ;
            o_prop.put(getAperetTaskTypes(pjo));

     JSONObject rules = new JSONObject();
     root.put(  "rules",rules);
        rules.put("connectionRules", new JSONArray());
        rules.put("cardinalityRules", new JSONArray());
		rules.put("containmentRules", new JSONArray());
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

        System.out.println(root);
    }

    private static String getDataFromServer() throws IOException {
        URL url = new URL("http://dreihund:19080/jbpm-gui-0.2/osgiex/steps?format=json");
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
    }

    private static JSONObject getAperteConf() throws JSONException {
            JSONObject o = new JSONObject();
            o.put("id","aperte-conf");
		    o.put("type","String");
		    o.put("title","Aperte Configuration");
		    o.put("description","Extended configuration for aperte reports");
		    o.put("readonly",false);
		    o.put("optional",true);
        return o;
    }
    private static JSONObject getAperteUrls(JSONArray pjo) throws JSONException {
            JSONObject o = new JSONObject();
            o.put("id","aperte-urlmap");
		    o.put("type","String");
		    o.put("title","Aperte function lis");
		    o.put("description","Url map");
		    o.put("readonly",false);
		    o.put("optional",true);


            JSONObject links = new JSONObject();
            links.put("User","http://dreihund:19080/jbpm-gui-0.2/step_editor");

            for(int i=0;i<pjo.length();i++){
                JSONObject oo = (JSONObject) pjo.get(i);
                String name = oo.get("name").toString();
//                try{
//                    String path= oo.get("path").toString();
//                    if(path.length()>0)
//                            links.put(name,path);
//                }catch(Exception e){
//                    System.err.println("Skipping step !!"+name+" e:"+e.getMessage());
//                }
            }
            o.put("value",links.toString());
        return o;
    }
     private static JSONObject getAperetTaskTypes(JSONArray pjo) throws JSONException {
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
        return o;
    }

}
