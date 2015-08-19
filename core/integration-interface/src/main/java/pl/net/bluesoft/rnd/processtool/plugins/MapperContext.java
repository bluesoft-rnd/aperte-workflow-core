package pl.net.bluesoft.rnd.processtool.plugins;

import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-11-03
 */
public class MapperContext {
	private final Map<String, Object> params = new HashMap<String, Object>();

	public Object getParam(String name) {
		return params.get(name);
	}

	public void setParam(String name, Object value) {
		params.put(name, value);
	}
}
