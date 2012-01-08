package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.signavio.platform.exceptions.RequestException;
import com.signavio.platform.util.StringUtil;


public class AperteRequestExceptionFilter implements Filter {

	private ServletContext servletContext;
	
	private void handleThrowable(Throwable t, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException  {
		t.printStackTrace();
		RequestException re = null;
		if(t instanceof RequestException) {
			re = (RequestException) t;
		} else if(t.getCause() instanceof RequestException) {
			re = (RequestException) t.getCause();
		}
		
		if(re != null) {
			res.setStatus(re.getHttpStatusCode());
		} else if (t instanceof SecurityException || t.getCause() instanceof SecurityException) {
			res.setStatus(403);
		} else {
			res.setStatus(500);
		}
		
//		Properties translation = TranslationFactory.getTranslation(req); 
		
		// this is very dirty since it requires 
		String message = null;
		if(re != null) {
			message = re.getErrorCode();
			if (message != null) {
				message = StringUtil.formatString(message, re.getParams());
			} else {
//				message = translation.getProperty("unknownError") + " (" + re.getErrorCode() + ")";
			}
		}
		
		if(message == null) {
//			message = translation.getProperty("unknownError") + " (" + t.getLocalizedMessage() + ")";
		}
		
		if (req.getHeader("Accept").contains("application/json")) 
		{
			JSONObject errorObject = new JSONObject();
			try {
				errorObject.put("message", message);
			} catch (JSONException e1) {
				e1.printStackTrace();
				throw new ServletException("Error Handling Failed", t);
			}
			res.getWriter().write(errorObject.toString());
		} else {
			req.setAttribute("message", message);
			servletContext.getRequestDispatcher("/WEB-INF/jsp/error.jsp").include(req, res);
		}
	}
	
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
	
		HttpServletRequest httpReq = (HttpServletRequest) req; 
		HttpServletResponse httpRes = (HttpServletResponse) res;
		
		try {
			chain.doFilter(req, res);
		} catch (Throwable t) {			
			handleThrowable(t, httpReq, httpRes);
		}
	}
	
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		this.servletContext = arg0.getServletContext();
		
	}
}
