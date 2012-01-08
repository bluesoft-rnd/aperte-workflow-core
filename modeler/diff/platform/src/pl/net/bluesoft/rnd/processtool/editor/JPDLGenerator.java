package pl.net.bluesoft.rnd.processtool.editor;

import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.net.bluesoft.rnd.processtool.editor.jpdl.exception.UnsupportedJPDLObjectException;
import pl.net.bluesoft.rnd.processtool.editor.jpdl.object.JPDLComponent;
import pl.net.bluesoft.rnd.processtool.editor.jpdl.object.JPDLObject;
import pl.net.bluesoft.rnd.processtool.editor.jpdl.object.JPDLTransition;
import pl.net.bluesoft.rnd.processtool.editor.jpdl.object.JPDLUserTask;
import pl.net.bluesoft.rnd.processtool.editor.jpdl.queue.QueueDef;
import pl.net.bluesoft.rnd.processtool.editor.jpdl.queue.QueueRight;

import com.signavio.platform.exceptions.RequestException;
import com.signavio.platform.util.fsbackend.FileSystemUtil;

public class JPDLGenerator {
  
	//key = resourceId
	private Map<String, JPDLComponent> componentMap = new HashMap<String, JPDLComponent>(); 
	private Map<String, JPDLTransition> transitionMap = new HashMap<String, JPDLTransition>();
	private SortedMap<Integer, QueueDef> queueConf;
	private final Logger logger = Logger.getLogger(JPDLGenerator.class);
	private String processName;
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public void generate(String json, String jpdlPath, String ptcPath, String queuePath) {
		try {
		  JSONObject jsonObj = new JSONObject(json);
		  processName = jsonObj.getJSONObject("properties").getString("name");
		  String queueConfJson = jsonObj.getJSONObject("properties").optString("queue-conf");
		  if (queueConfJson != null && queueConfJson.trim().length() > 0) {
		    queueConf = mapper.readValue(queueConfJson, new TypeReference<SortedMap<Integer,QueueDef>>(){});
		  } else {
			queueConf = new TreeMap<Integer, QueueDef>();
		  }
		  JSONArray childShapes = jsonObj.getJSONArray("childShapes");
		  for (int i = 0; i < childShapes.length(); i++) {
			  JSONObject obj = childShapes.getJSONObject(i);
			  JPDLObject jpdlObject = JPDLObject.getJPDLObject(obj);
			  jpdlObject.fillBasicProperties(obj);
			  if (jpdlObject instanceof JPDLComponent)
			    componentMap.put(jpdlObject.getResourceId(), (JPDLComponent)jpdlObject);
			  else if (jpdlObject instanceof JPDLTransition)
				transitionMap.put(jpdlObject.getResourceId(), (JPDLTransition)jpdlObject);
		  }
		} catch (JSONException e) {
			logger.error("Error while generating JPDL file.", e);
			throw new RequestException("Error while generating JPDL file.", e);
		} catch (UnsupportedJPDLObjectException e) {
			logger.error("Error while generating JPDL file.", e);
			throw new RequestException(e.getMessage());
		} catch (JsonMappingException e) {
			logger.error("Error while parsing queues.", e);
			throw new RequestException(e.getMessage());
		} catch (JsonParseException e) {
			logger.error("Error while parsing queues.", e);
			throw new RequestException(e.getMessage());
		} catch (IOException e) {
			logger.error("Error while parsing queues.", e);
			throw new RequestException(e.getMessage());
		}
		
		//drugie przejscie, uzupelnienie mapy tranzycji
		for (String key : componentMap.keySet()) {
			JPDLComponent cmp = componentMap.get(key);
			for (String resourceId : cmp.getOutgoing().keySet()) {
				JPDLTransition transition = transitionMap.get(resourceId);
				transition.setTargetName(componentMap.get(transition.getTarget()).getName());
				cmp.putTransition(resourceId, transition);
			}
		}
		
		generateJpdlFile(jpdlPath);
		generatePtcFile(ptcPath);
		generateQueueFile(queuePath);
	
	}
	
	private void generateJpdlFile(String jpdlPath) {
		StringBuffer jpdl = new StringBuffer();
		jpdl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		jpdl.append(String.format("<process name=\"%s\" xmlns=\"http://jbpm.org/4.4/jpdl\">\n", processName));

		Set<String> swimlanes = new HashSet<String>();
		
		for (String key : componentMap.keySet()) {
			JPDLComponent comp = componentMap.get(key);
			if (comp instanceof JPDLUserTask) {
				JPDLUserTask userTask = (JPDLUserTask)comp;
				if (userTask.getSwimlane() != null && userTask.getSwimlane().trim().length() > 0) {
					swimlanes.add(userTask.getSwimlane());
				}
			}
		}
		
		Iterator<String> i = swimlanes.iterator();
		while (i.hasNext()) {
			String sl = i.next();
			jpdl.append(String.format("<swimlane candidate-groups=\"%s\" name=\"%s\"/>\n", sl, sl));
		}
		  
		
		for (String key : componentMap.keySet()) {
			jpdl.append(componentMap.get(key).toXML());
		}
		
		jpdl.append("</process>");
		
		FileSystemUtil.deleteFileOrDirectory(jpdlPath);
		FileSystemUtil.createFile(jpdlPath, jpdl.toString());
	}
	
	private void generatePtcFile(String ptcPath) {
        StringBuffer ptc = new StringBuffer();
        ptc.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        ptc.append(String.format("<config.ProcessDefinitionConfig bpmDefinitionKey=\"%s\" description=\"%s\" processName=\"%s\" comment=\"%s\">\n", processName, processName, processName, processName));
        ptc.append("<states>\n");
        
		//generowanie process-tool-config'a
		for (String key : componentMap.keySet()) {
			JPDLComponent cmp = componentMap.get(key);
			if (cmp instanceof JPDLUserTask) {
				JPDLUserTask task = (JPDLUserTask)cmp;
				if (task.getWidget() != null) {
					ptc.append(task.generateWidgetXML());
				}
			}
		}
		
		ptc.append("</states>\n");
		ptc.append("</config.ProcessDefinitionConfig>\n");
		FileSystemUtil.deleteFileOrDirectory(ptcPath);
		FileSystemUtil.createFile(ptcPath, ptc.toString());
	}
	
	private void generateQueueFile(String queuePath) {
		
		StringBuffer q = new StringBuffer();
		q.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		q.append("<list>\n");
		
		for (Integer i : queueConf.keySet()) {
			QueueDef qd = queueConf.get(i);
			q.append(String.format("<config.ProcessQueueConfig name=\"%s\" description=\"%s\">\n", qd.getName(), qd.getDescription()));
			SortedMap<Integer,QueueRight> rights = qd.getRights();
			q.append("<rights>\n");
			for (Integer j : rights.keySet()) {
			  QueueRight qr = rights.get(j);
			  q.append(String.format("<config.ProcessQueueRight roleName=\"%s\" browseAllowed=\"%b\"/>\n", qr.getRoleName(), qr.isBrowseAllowed()));
			}
			q.append("</rights>\n");
			q.append("</config.ProcessQueueConfig>\n");
		}
		
		q.append("</list>\n");
		FileSystemUtil.deleteFileOrDirectory(queuePath);
		FileSystemUtil.createFile(queuePath, q.toString());
	}
	/*public static String readFile(String fname) {
		StringBuffer sb = new StringBuffer();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(fname));
		    String str;
		    while ((str = in.readLine()) != null) {
		        sb.append(str);
		    }
		    in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	
    public static void main(String[] args) {
	   new JPDLGenerator().generate(
			  readFile("c:\\LB\\bls_aperteworkflow\\maintest.json"),
			  "c:\\LB\\bls_aperteworkflow\\out.jpdl.xml",
			  "c:\\LB\\bls_aperteworkflow\\out.process-tool-config.xml"
		 );
    }*/
}
