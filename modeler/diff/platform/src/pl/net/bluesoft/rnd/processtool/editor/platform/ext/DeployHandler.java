package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import com.signavio.platform.annotations.HandlerConfiguration;
import com.signavio.platform.annotations.HandlerMethodActivation;
import com.signavio.platform.core.Platform;
import com.signavio.platform.core.PlatformProperties;
import com.signavio.platform.exceptions.RequestException;
import com.signavio.platform.handler.BasisHandler;
import com.signavio.platform.security.business.FsAccessToken;
import com.signavio.platform.security.business.FsSecurityManager;
import com.signavio.warehouse.directory.business.FsDirectory;
import com.signavio.warehouse.model.business.ModelTypeFileExtension;
import com.signavio.warehouse.model.business.ModelTypeManager;
import com.signavio.warehouse.model.business.modeltype.SignavioModelType;
import com.signavio.warehouse.revision.business.RepresentationType;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

@HandlerConfiguration(uri = "/deploy", rel="deploy")
public class DeployHandler extends BasisHandler {

	public DeployHandler(ServletContext servletContext) {
		super(servletContext);
	}

	private Manifest getManifest(String bundleName, String bundleDescription, String processToolDeployment) {
		Manifest mf = new Manifest();
		mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		mf.getMainAttributes().put(new Attributes.Name("Created-By"), "Aperte Modeler");
		mf.getMainAttributes().put(new Attributes.Name("Built-By"), "Aperte Modeler");
		mf.getMainAttributes().put(new Attributes.Name("Bundle-ManifestVersion"), "2");
		mf.getMainAttributes().put(new Attributes.Name("Bundle-SymbolicName"), processToolDeployment);
		mf.getMainAttributes().put(new Attributes.Name("Bundle-Version"), "1");
		mf.getMainAttributes().put(new Attributes.Name("Bundle-Name"), bundleName);
		mf.getMainAttributes().put(new Attributes.Name("Bundle-Description"), bundleDescription);
		mf.getMainAttributes().put(new Attributes.Name("Import-Package"), "org.osgi.framework");
		mf.getMainAttributes().put(new Attributes.Name("ProcessTool-Process-Deployment"), processToolDeployment);
		return mf;
	}
	
	private byte[] svg2png(byte[] svg) throws TranscoderException {
		ByteArrayInputStream bais = new ByteArrayInputStream(svg);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		TranscoderInput input = new TranscoderInput(bais);
		TranscoderOutput output = new TranscoderOutput(baos);
		new PNGTranscoder().transcode(input, output);
		byte[] png = baos.toByteArray();
		return png;
	}
    
	private void addPackageDirs(String packageName, JarOutputStream target) throws IOException {
		String[] packageElems = packageName.split("\\.");
		String[] dirNames = new String[packageElems.length];
		for (int i = 0; i < packageElems.length; i++) {
		   dirNames[i] = "";
		   for (int j = 0; j <= i; j++) {
			   dirNames[i] = dirNames[i] + packageElems[j] + "/";
		   }
		}
		for (String d : dirNames) {
			JarEntry entry = new JarEntry(d);
			entry.setTime(new Date().getTime());
			target.putNextEntry(entry);
			target.closeEntry();
		}
	}
	
	private void addEntry(String entryName, JarOutputStream target, InputStream in) throws IOException {
		BufferedInputStream bin = null;
		try {  
            JarEntry entry = new JarEntry(entryName);
            entry.setTime(new Date().getTime());
            target.putNextEntry(entry);
            bin = new BufferedInputStream(in);
            byte[] buffer = new byte[1024];
            while (true) {
            int count = in.read(buffer);
            if (count == -1)
                break;
            target.write(buffer, 0, count);
            }
            target.closeEntry();
            bin.close();
		} finally {
			if (bin != null) {
                bin.close();
            }
		}
	}
	
	@Override
	@HandlerMethodActivation
	public Object postRepresentation(Object params, FsAccessToken token) {
		JSONObject jsonParams = (JSONObject) params;
		
		try {
            String name = jsonParams.getString("name");
            String parentId = jsonParams.getString("parent");
            parentId = parentId.replace("/directory/", "");
            FsDirectory parent = FsSecurityManager.getInstance().loadObject(FsDirectory.class, parentId, token);
            String signavioXMLExtension = SignavioModelType.class.getAnnotation(ModelTypeFileExtension.class).fileExtension();
            String fileName = name + signavioXMLExtension;
            String fileNameWithPath = parent.getPath() + File.separator + fileName;

            byte [] jsonData = ModelTypeManager.getInstance().getModelType(signavioXMLExtension).getRepresentationInfoFromModelFile(RepresentationType.JSON, fileNameWithPath);
            byte [] svgData = ModelTypeManager.getInstance().getModelType(signavioXMLExtension).getRepresentationInfoFromModelFile(RepresentationType.SVG, fileNameWithPath);
            String jsonRep = new String(jsonData, "utf-8");
            JSONObject jsonObj = new JSONObject(jsonRep);

            // MANIFEST
            String processFileName = jsonObj.getJSONObject("properties").optString("aperte-process-filename");
            String processVersion = jsonObj.getJSONObject("properties").optString("aperte-process-version");

            String bundleDesc = jsonObj.getJSONObject("properties").optString("mf-bundle-description");
            String bundleName = jsonObj.getJSONObject("properties").optString("mf-bundle-name");
            String processToolDeployment = jsonObj.getJSONObject("properties").optString("mf-processtool-deployment");

            if (isEmpty(bundleDesc) || isEmpty(bundleName)
            || isEmpty(processToolDeployment) || isEmpty(processFileName) || isEmpty(processVersion)) {
              throw new RequestException("Diagram attributes: Aperte process filename, Aperte process version number, Manifest: Bundle-Name, Manifest: Bundle-Description, Manifest: ProcessTool-Process-Deployment must not be empty.");
            }
            Manifest mf = getManifest(bundleName, bundleDesc, processToolDeployment);

            // convert SVG to PNG format
            byte[] png = svg2png(svgData);

            // create new temporary JAR
            File tempJar = File.createTempFile("jar", null, new File(parent.getPath()));
            JarOutputStream target = new JarOutputStream(new FileOutputStream(tempJar), mf);
            addPackageDirs(processToolDeployment, target);

            String processDir = processToolDeployment.replace('.','/') + '/';

            // adding PNG and XML files
            addEntry(processDir + "processdefinition.png", target, new ByteArrayInputStream(png));
            addEntry(processDir + "processdefinition.jpdl.xml", target, new FileInputStream(parent.getPath() + File.separator + name + ".jpdl"));
            addEntry(processDir + "processtool-config.xml", target, new FileInputStream(parent.getPath() + File.separator + name + ".processtool-config.xml"));
            addEntry(processDir + "queues-config.xml", target, new FileInputStream(parent.getPath() + File.separator + name + ".queues-config.xml"));

            // close the JAR
            target.close();

            // copy to osgi-plugins
            PlatformProperties props =  Platform.getInstance().getPlatformProperties();
            String osgiPluginsDir = props.getAperteOsgiPluginsDir();
            copy(tempJar, new File(osgiPluginsDir + File.separator + processFileName + "-" + processVersion + ".jar"));

            // delete temporary directory
            tempJar.delete();
		  
		} catch (JSONException e) {
			throw new RequestException("JSONException", e);
		} catch (UnsupportedEncodingException e) {
			throw new RequestException("UnsupportedEncodingException", e);
		} catch (TranscoderException e) {
			throw new RequestException("Error while creating PNG file", e);
		} catch (IOException e) {
			throw new RequestException("Error while creating JAR file", e);
		}

		return "OK";
	}
	
    private boolean isEmpty(String s) {
    	return s == null || s.trim().length() == 0;
    }
    
    private void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}

}
