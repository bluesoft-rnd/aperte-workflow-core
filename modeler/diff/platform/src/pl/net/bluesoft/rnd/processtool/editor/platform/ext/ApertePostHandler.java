package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import com.signavio.platform.annotations.HandlerConfiguration;
import com.signavio.platform.handler.BasisHandler;
import com.signavio.platform.security.business.FsAccessToken;
import com.signavio.platform.security.business.FsSecureBusinessObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

@HandlerConfiguration(uri="/aperte_post", rel="aperte")
public class ApertePostHandler extends BasisHandler
{
  public ApertePostHandler(ServletContext servletContext)
  {
    super(servletContext);
  }

  public <T extends FsSecureBusinessObject> void doPost(HttpServletRequest req, HttpServletResponse res, FsAccessToken token, T sbo)
  {
    res.setStatus(200);
    res.setContentType("text/html");

    JSONObject jParams = (JSONObject)req.getAttribute("params");
    
    String stepEditor = jParams.optString("step_editor");
    String queueEditor = jParams.optString("queue_editor");
    
    PrintWriter out = null;
    try {
      out = res.getWriter();
      if (stepEditor != null && stepEditor.trim().length() > 0)
        out.println("<html><head></head><body><script type=\"text/javascript\">    window.parent.editorSetData(\"" + stepEditor + "\"); " + "</script></body></html>");
      else if (queueEditor != null && queueEditor.trim().length() > 0)
        out.println("<html><head></head><body><script type=\"text/javascript\">    window.parent.editorSetQueueData(\"" + queueEditor + "\"); " + "</script></body></html>");

      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}