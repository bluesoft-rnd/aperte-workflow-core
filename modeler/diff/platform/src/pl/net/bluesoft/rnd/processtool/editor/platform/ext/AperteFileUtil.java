package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import pl.net.bluesoft.rnd.processtool.editor.JPDLGenerator;
import de.hpi.bpmn2_0.exceptions.BpmnConverterException;

public class AperteFileUtil {
	public static void storeJpdlFile(String pathPrefix, String jsonRep) throws IOException, JSONException, BpmnConverterException, JAXBException, SAXException, ParserConfigurationException, TransformerException {
		String jpdlPath = pathPrefix + ".jpdl";
		String ptcPath = pathPrefix + ".processtool-config.xml";
		String queuePath = pathPrefix + ".queues-config.xml";
		new JPDLGenerator().generate(jsonRep, jpdlPath, ptcPath, queuePath);
	}
}
