package pl.net.bluesoft.rnd.pt.ext.report.util;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Base64;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.pt.ext.report.model.ReportDAO;
import pl.net.bluesoft.rnd.pt.ext.report.model.ReportTemplate;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class JasperReportingUtil {

    private static final Logger logger = Logger.getLogger(JasperReportingUtil.class.getName());
    
	public static Object attributeByKeyPath(ProcessInstance pi, String key, String path) {
		ProcessInstanceAttribute attributeByKey = findAttributeByKey(pi, key);
		return expandPath(path, attributeByKey);
	}
	public static Object attributeByClassPath(ProcessInstance pi, String name, String path) {
		ProcessInstanceAttribute attributeByKey = findAttributeByClassName(pi, name);
		return expandPath(path, attributeByKey);
	}
	public static Object expandPath(String path, Object o) {
		if (o == null) return null;
		try {
			return PropertyUtils.getProperty(o, path);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static ProcessInstanceAttribute findAttributeByKey(ProcessInstance pi, String key) {
		for (ProcessInstanceAttribute attr: pi.getProcessAttributes()) {
			if (attr.getKey() != null && attr.getKey().equals(key)) {
				return attr;
			}
		}
		return null;
	}

	public static ProcessInstanceAttribute findAttributeByClassName(ProcessInstance pi, String name) {
		for (ProcessInstanceAttribute attr: pi.getProcessAttributes()) {
			if (attr.getClass().getName().equals(name)) {
				return attr;
			}
		}
		return null;
	}

    public static JasperReport getReport(String reportName) {
        JasperReport jasperReport = null;
/*        ClassLoader previousLoader = null;
        Thread t = Thread.currentThread();*/
        try {
            ReportTemplate template = new ReportDAO().loadByName(reportName);
            if (template == null)
                throw new Exception("Report template does not exist for name: ".concat(reportName));
            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
            ByteArrayInputStream contentInputStream = new ByteArrayInputStream(Base64.decodeBase64(
                    (new String(template.getContent())).getBytes("UTF-8")));
//            previousLoader = t.getContextClassLoader();
//            ClassLoader newClassLoader = ctx.getRegistry().getModelAwareClassLoader(
//                    JasperReportingUtil.class.getClassLoader());
//            t.setContextClassLoader(newClassLoader);
            jasperReport = JasperCompileManager.compileReport(contentInputStream);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
//        } finally {
//            if (previousLoader != null)
//                t.setContextClassLoader(previousLoader);
        }
        return jasperReport;
    }
}
