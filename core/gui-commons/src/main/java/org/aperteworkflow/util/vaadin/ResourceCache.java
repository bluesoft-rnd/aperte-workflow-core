package org.aperteworkflow.util.vaadin;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-03-01
 * Time: 13:05
 */
public class ResourceCache {
	private final Map<String, Resource> resourceCache = new HashMap<String, Resource>();
	private final Application application;

	public ResourceCache(Application application) {
		this.application = application;
	}

	public void cacheResource(String path, Resource resource) {
		resourceCache.put(path, resource);
	}

	public Resource getResource(String path) {
		return resourceCache.get(path);
	}

	public Resource getImage(String path) {
		if (!resourceCache.containsKey(path)) {
			resourceCache.put(path, new ClassResource(getClass(), path, application));
		}
		return resourceCache.get(path);
	}
}
