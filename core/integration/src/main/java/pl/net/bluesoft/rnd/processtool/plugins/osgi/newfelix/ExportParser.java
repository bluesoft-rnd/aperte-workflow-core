package pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 16:13
 */
class ExportParser {
	private final String s;
	private int pos;

	public ExportParser(String s) {
		this.s = nvl(s);
		this.pos = 0;
	}

	private void eatWhiteSpaces() {
		while (!eot() && Character.isWhitespace(curChar())) {
			++pos;
		}
	}

	private boolean eot() {
		return pos >= s.length();
	}

	private char curChar() {
		return s.charAt(pos);
	}

	public Map<String, Map<String, String>> parse() {
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
		eatWhiteSpaces();
		while (!eot()) {
			String pack = packageName();
			result.put(pack, new HashMap<String, String>());
			if (eot()) {
				break;
			}
			if (curChar() == ',') {
				++pos;
			}
			else if (curChar() == ';') {
				++pos;
				result.put(pack, additionalArgs());
			}
		}
		return result;
	}

	public Set<String> parsePackageNamesOnly() {
		return parse().keySet();
	}

	private Map<String, String> additionalArgs() {
		Map<String, String> result = new HashMap<String, String>();

		while (!eot()) {
			eatWhiteSpaces();
			String arg = additionalArg().trim();
			Pattern p1 = Pattern.compile("^(.*?):?=(.*)$");
			Matcher m = p1.matcher(arg);
			if (m.find()) {
				String r = m.group(2).trim();
				if (r.startsWith("\"") && r.endsWith("\"")) {
					r = r.substring(1, r.length() - 1).trim();
				}
				result.put(m.group(1).trim(), r);
			}
			if (eot()) {
				break;
			}
			else if (curChar() == ',') {
				++pos;
				break;
			}
			else if (curChar() == ';') {
				++pos;
			}
		}
		return result;
	}

	private String additionalArg() {
		int start = pos;
		while (!eot()) {
			char c = curChar();
			if (c == ',' || c == ';') {
				break;
			}
			++pos;
			if (c == '\"') {
				while (!eot() && curChar() != '\"') {
					++pos;
				}
				if (!eot() && curChar() == '\"') {
					++pos;
				}
			}
		}
		return s.substring(start, pos);
	}

	private String packageName() {
		int start = pos;
		while (!eot() && curChar() != ',' && curChar() != ';') {
			++pos;
		}
		return s.substring(start, pos);
	}
}