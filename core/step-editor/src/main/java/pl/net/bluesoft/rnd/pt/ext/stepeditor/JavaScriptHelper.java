package pl.net.bluesoft.rnd.pt.ext.stepeditor;

import com.vaadin.ui.Window;

import java.text.MessageFormat;
import java.util.logging.Logger;

public class JavaScriptHelper {

    private static final Logger logger = Logger.getLogger(JavaScriptHelper.class.getName());
    
	private static final String	CALLBACK_FUNCTION		= "window.parent.opener.editorSetData(\"{0}\");";
	private static final String	CLOSE_FUNCTION			= "self.close();";
	private static final String	CLOSE_PREVENT_FUNCTION	= "	 window.onbeforeunload = confirmExit; " + "function confirmExit(){ "																	+ "return \"Are you sure you want to navigate away from this page?\";}";
	private static final String POST_TO_URL_FUNCTION    = "function post_to_url(path, params, method) {\r\n" +
			"    method = method || \"post\"; // Set method to post by default, if not specified.\r\n" + 
			"\r\n" + 
			"    // The rest of this code assumes you are not using a library.\r\n" + 
			"    // It can be made less wordy if you use one.\r\n" + 
			"    var form = document.createElement(\"form\");\r\n" + 
			"    form.setAttribute(\"method\", method);\r\n" + 
			"    form.setAttribute(\"action\", path);\r\n" + 
			"\r\n" + 
			"    for(var key in params) {\r\n" + 
			"        var hiddenField = document.createElement(\"input\");\r\n" + 
			"        hiddenField.setAttribute(\"type\", \"hidden\");\r\n" + 
			"        hiddenField.setAttribute(\"name\", key);\r\n" + 
			"        hiddenField.setAttribute(\"value\", params[key]);\r\n" + 
			"\r\n" + 
			"        form.appendChild(hiddenField);\r\n" + 
			"    }\r\n" + 
			"\r\n" + 
			"    document.body.appendChild(form);\r\n" + 
			"    form.submit();\r\n" + 
			"}\r\n" + 
			"" ;

	private static final String	CLOSE_ALLOW_FUNCTION		= "	window.onbeforeunload = null;";
	private static final String	CALL_POST_TO_URL_FUNCTION	= " post_to_url(\"{0}\", {1});";

    private Window	window;
	
	public JavaScriptHelper(Window window) {
		this.window = window;
		executeScript(POST_TO_URL_FUNCTION);
	}

	public void preventWindowClosing() {
		executeScript(CLOSE_PREVENT_FUNCTION);
	}

	public void allowWindowClosing() {
		executeScript(CLOSE_ALLOW_FUNCTION);
	}

	public void postAndRedirectStep(String url, String jsonConfig) {
        executeScript(CLOSE_ALLOW_FUNCTION);
        String escapedJson = escapeJsonForScript(jsonConfig);
        executeScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"step_editor\": \"" + escapedJson + "\"}")));
	}

	public void postAndRedirectAction(String url, String jsonConfig) {
		executeScript(CLOSE_ALLOW_FUNCTION);
        String escapedJson = escapeJsonForScript(jsonConfig);
		executeScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"action_editor\": \"" + escapedJson + "\"}")));
	}

    public void postAndRedirectProcess(String url, String jsonConfig) {
        executeScript(CLOSE_ALLOW_FUNCTION);
        String escapedJson = escapeJsonForScript(jsonConfig);
        executeScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"process_editor\": \"" + escapedJson + "\"}")));
    }

    /**
     * Execute the JavaScript
     *
     * @param script Script content
     */
    private void executeScript(String script) {
        logger.fine("Executing javascript: `" + script + "`");
        window.executeJavaScript(script);
    }
    
    /**
     * Escape the JSON string so it can be displayed as part of JavaScript in HTML document
     *
     * @param json JSON string
     * @return  Escaped JSON string
     */
    private static String escapeJsonForScript(String json) {
        return json.replaceAll("\"", "\\\\\"");
    }

}
