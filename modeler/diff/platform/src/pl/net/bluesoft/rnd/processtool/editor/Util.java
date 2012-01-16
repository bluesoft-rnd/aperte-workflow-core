package pl.net.bluesoft.rnd.processtool.editor;

public class Util {
	public static String replaceXmlEscapeCharacters(String input) {
		return input.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
				.replaceAll("&quot;", "\"").replaceAll("&#039;", "\'")
				.replaceAll("&amp;", "&");
	}
}
