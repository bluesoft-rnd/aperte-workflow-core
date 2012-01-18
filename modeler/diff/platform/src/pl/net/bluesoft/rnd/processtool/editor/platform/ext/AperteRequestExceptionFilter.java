package pl.net.bluesoft.rnd.processtool.editor.platform.ext;

import com.signavio.platform.exceptions.RequestException;
import com.signavio.platform.util.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to translate the exception messages
 */
public class AperteRequestExceptionFilter implements Filter {

    private static final String SIGNAVIO_REQUEST_EXPECTION_PREFIX = "RequestException Error Code: ";
    
	private ServletContext servletContext;
    private AperteMessageBundle messages;
	
	private void handleThrowable(Throwable t, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException  {
		RequestException re = null;
		if(t instanceof RequestException) {
			re = (RequestException) t;
		} else if(t.getCause() instanceof RequestException) {
			re = (RequestException) t.getCause();
		}

        // Prepare response and message
        String message = null;
		if (re != null) {
			res.setStatus(re.getHttpStatusCode());
            StringBuilder builder = new StringBuilder();
            describeException(re, builder);
            message = builder.toString();
		} else if (t instanceof SecurityException || t.getCause() instanceof SecurityException) {
			res.setStatus(403);
        } else {
			res.setStatus(500);
		}

        // Send response and message
		if (req.getHeader("Accept").contains("application/json")) {
			JSONObject errorObject = new JSONObject();
			try {
				errorObject.put("message", message);
			} catch (JSONException e1) {
				throw new ServletException("Error Handling Failed", t);
			}
			res.getWriter().write(errorObject.toString());
		} else {
    		req.setAttribute("message", message);
			servletContext.getRequestDispatcher("/WEB-INF/jsp/error.jsp").include(req, res);
		}
	}
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
	
		HttpServletRequest httpReq = (HttpServletRequest) req; 
		HttpServletResponse httpRes = (HttpServletResponse) res;
		
		try {
			chain.doFilter(req, res);
		} catch (Throwable t) {			
			handleThrowable(t, httpReq, httpRes);
		}
	}

	public void destroy() {

	}

	public void init(FilterConfig fc) throws ServletException {
		servletContext = fc.getServletContext();

        messages = new AperteMessageBundle();
        messages.addPropertiesFromClasspath("/signavio-error-codes.properties");
	}
    
    private void describeException(Throwable t, StringBuilder builder) {
        if (t != null) {
            if (t instanceof RequestException) {
                // handle signavio exception object
                RequestException re = (RequestException) t;
                if (re.getErrorCode() != null) {
                    builder.append(StringUtil.formatString(messages.getMessage(re.getErrorCode()), re.getParams()));
                } else {
                    builder.append("Unknown error code (" + re.getClass().getName() + ").s");
                }
            } else {
                // handle unknown object
                // make sure to remove the content signavio is adding to the message
                builder.append(removeSignavioExceptionPrefix(t.getLocalizedMessage()));
            }

            if (builder.lastIndexOf(".") != builder.length() - 1) {
                builder.append('.');
            }
            
            if (t.getCause() != null) {
                builder.append(' ');
                describeException(t.getCause(), builder);
            }
        }
    }
    
    private String handleMessage(String message) {
        
        return message;
    }
    
    private String removeSignavioExceptionPrefix(String message) {
        if (message.startsWith(SIGNAVIO_REQUEST_EXPECTION_PREFIX)) {
            return message.substring(SIGNAVIO_REQUEST_EXPECTION_PREFIX.length());
        } else {
            return message;
        }
    }

}
