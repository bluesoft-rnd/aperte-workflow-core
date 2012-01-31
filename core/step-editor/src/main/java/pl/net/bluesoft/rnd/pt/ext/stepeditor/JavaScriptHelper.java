package pl.net.bluesoft.rnd.pt.ext.stepeditor;

import com.vaadin.ui.Window;

import java.text.MessageFormat;

public class JavaScriptHelper {

	private static final String		CALLBACK_FUNCTION		= "window.parent.opener.editorSetData(\"{0}\");";
//	private static final String		CALLBACK_FUNCTION_TMP	= "window.parent.opener.editorSetData();";
	private static final String		CLOSE_FUNCTION			= "self.close();";
	private static final String		CLOSE_PREVENT_FUNCTION	= "	 window.onbeforeunload = confirmExit; " + "function confirmExit(){ "
																	+ "return \"Are you sure you want to navigate away from this page?\";}";
	private static final String POST_TO_URL_FUNCTION = "function post_to_url(path, params, method) {\r\n" + 
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
		window.executeJavaScript(POST_TO_URL_FUNCTION);
	}

	public void preventWindowClosing() {
		window.executeJavaScript(CLOSE_PREVENT_FUNCTION);
	}
	public void allowWindowClosing() {
		window.executeJavaScript(CLOSE_ALLOW_FUNCTION);
	}
	public void closeWindow(String url) {
//		window.executeJavaScript(CLOSE_FUNCTION);
		System.out.println(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, "{\"step_editor\" : \"\"}"));
		window.executeJavaScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, "{\"step_editor\" : \"\"}"));
	}
	public void callbackFunction(String jsonConfig) {
		System.out.println(MessageFormat.format(CALLBACK_FUNCTION, ("{\"step_editor\":" + jsonConfig + "}").replaceAll("\"", "\\\\\"")));
		window.executeJavaScript(MessageFormat.format(CALLBACK_FUNCTION, ("{\"step_editor\":" + jsonConfig + "}").replaceAll("\"", "\\\\\"")));
	}
	public void postAndRedirectStep(String url, String jsonConfig) {
		window.executeJavaScript(CLOSE_ALLOW_FUNCTION);
		System.out.println(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"step_editor\": \"" + jsonConfig.replaceAll("\"", "\\\\\"") + "\"}")));
		window.executeJavaScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"step_editor\": \"" + jsonConfig.replaceAll("\"", "\\\\\"") + "\"}")));
	}
	public void postAndRedirectQueue(String url, String jsonConfig) {
		window.executeJavaScript(CLOSE_ALLOW_FUNCTION);
		System.out.println(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"queue_editor\": \"" + jsonConfig.replaceAll("\"", "\\\\\"") + "\"}")));
		window.executeJavaScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"queue_editor\": \"" + jsonConfig.replaceAll("\"", "\\\\\"") + "\"}")));
	}
	public void postAndRedirectAction(String url, String jsonConfig) {
		window.executeJavaScript(CLOSE_ALLOW_FUNCTION);
		System.out.println(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"action_editor\": \"" + jsonConfig.replaceAll("\"", "\\\\\"") + "\"}")));
		window.executeJavaScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"action_editor\": \"" + jsonConfig.replaceAll("\"", "\\\\\"") + "\"}")));
	}

    public void postAndRedirectProcess(String url, String processConfig) {
        window.executeJavaScript(CLOSE_ALLOW_FUNCTION);
        String escapedJson = processConfig.replaceAll("\"", "\\\\\"");
        window.executeJavaScript(MessageFormat.format(CALL_POST_TO_URL_FUNCTION, url, ("{\"process_editor\": \"" + escapedJson + "\"}")));
    }

}
