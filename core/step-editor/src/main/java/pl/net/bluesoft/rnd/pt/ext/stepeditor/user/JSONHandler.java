package pl.net.bluesoft.rnd.pt.ext.stepeditor.user;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Tree;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.aperteworkflow.editor.domain.Permission;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.Messages;
import pl.net.bluesoft.rnd.pt.ext.stepeditor.TaskConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JSONHandler {

    private static final Logger logger = Logger.getLogger(JSONHandler.class.getName());
    
	private static final String WIDGET_ID = "widgetId";
    private static final String PRIORITY = "priority";
	private static final String PROPERTIES = "properties";
	private static final String PERMISSIONS = "permissions";
	private static final String STEP_PERMISSIONS = "step-permissions";
	private static final String CHILDREN = "children";
	private static final String NAME = "name";

	public static final String ASSIGNEE = "assignee";
	public static final String SWIMLANE = "swimlane";
	public static final String CANDIDATE_GROUPS = "candidate_groups";

	public static class ParsingFailedException extends Exception {
		public ParsingFailedException(Exception e) {
			super(e);
		}
	}

	public static class WidgetNotFoundException extends Exception {
		String widgetItemName;
		public WidgetNotFoundException(String widgetItemName) {
			this.widgetItemName = widgetItemName;
		}
		public String getWidgetItemName() {
			return widgetItemName;
		}
	}

	private static final ObjectMapper mapper = new ObjectMapper();

	public static Map<String, String> loadConfig(HierarchicalContainer hc, WidgetItemInStep rootItem, 
                                                 String config, List<Permission> permissions) throws WidgetNotFoundException, ParsingFailedException {
		try {
			Map<String, Object> map = mapper.readValue(config, Map.class);

			analyzeChildren(map, hc, rootItem);
			
			HashMap<String, String> resultMap = new HashMap<String, String>();
			if(map.containsKey(ASSIGNEE)){
				resultMap.put(ASSIGNEE, map.get(ASSIGNEE).toString());
			}
			if(map.containsKey(SWIMLANE)){
				resultMap.put(SWIMLANE, map.get(SWIMLANE).toString());
			}
			if(map.containsKey(CANDIDATE_GROUPS)){
				resultMap.put(CANDIDATE_GROUPS, map.get(CANDIDATE_GROUPS).toString());
			}
            permissions.clear();
            if (map.containsKey(STEP_PERMISSIONS)) {
                permissions.addAll(permissions);
            }
			return resultMap;

		} catch (JsonParseException e) {
			logger.log(Level.SEVERE, "Error loading configuration", e);
			throw new ParsingFailedException(e);
		} catch (JsonMappingException e) {
            logger.log(Level.SEVERE, "Error loading configuration", e);
			throw new ParsingFailedException(e);
		} catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading configuration", e);
			throw new ParsingFailedException(e);
		}
	}

	private static void analyzeChildren(Map<String, Object> map, HierarchicalContainer hc, WidgetItemInStep rootItem) throws WidgetNotFoundException {
		Collection<Map<String, Object>> children = (Collection<Map<String, Object>>) map.get(CHILDREN);
		if (children != null) {
			for (Map<String, Object> node : children) {
				WidgetItemInStep newItem = analyzeNode(node, hc, rootItem);
				analyzeChildren(node, hc, newItem);
			}
		}
	}

	private static WidgetItemInStep analyzeNode(Map<String, Object> node, HierarchicalContainer hc, WidgetItemInStep rootItem) throws WidgetNotFoundException {
		String widgetItemName = (String) node.get(WIDGET_ID);
		WidgetItem widgetItem = WidgetItem.getWidgetItem(widgetItemName);
		if (widgetItem == null) {
			throw new WidgetNotFoundException(widgetItemName);
		}
		final WidgetItemInStep item = new WidgetItemInStep(widgetItem);

		Map<String, Object> properties = (Map<String, Object>) node.get(PROPERTIES);
		if (properties != null) {
			for (String key : properties.keySet()) {
				for (Property<?> property : item.getProperties()) {
					if (property.getPropertyId().equals(key)) {
						property.setValue(new String(Base64.decodeBase64(properties.get(key).toString())));
					}
				}
			}
		}

		Map<String, Object> permissions = (Map<String, Object>) node.get(PERMISSIONS);
		if (permissions != null) {
			for (String key : permissions.keySet()) {
				for (Property<?> perm : item.getPermissions()) {
					if (perm.getPropertyId().equals(key)) {
						perm.setValue(permissions.get(key));
					}
				}
			}
		}
		
		Item newItem = hc.addItem(item);
		newItem.getItemProperty(NAME).setValue(widgetItem.getName());
		hc.setParent(item, rootItem);
		hc.setChildrenAllowed(item, widgetItem.getChildrenAllowed());

		return item;
	}

	static Map<String, Object> collectNode(final Tree tree, Object node, Integer priority) {
		Map<String, Object> map = new HashMap<String, Object>();
		final WidgetItemInStep widgetItemInStep = (WidgetItemInStep) node;

		Map<String, Object> propertiesMap = new HashMap<String, Object>();
		if (widgetItemInStep.hasProperties()) {
			for (Property<?> property : widgetItemInStep.getProperties()) {
				if (property.getValue() != null) {
                    String encoded = Base64.encodeBase64URLSafeString(property.getValue().toString().getBytes());
					propertiesMap.put(property.getPropertyId(), encoded);
                }
			}
		}
		Map<String, Object> permissionsMap = new HashMap<String, Object>();
		if (widgetItemInStep.hasPermissions()) {
			for (Property<?> perm : widgetItemInStep.getPermissions()) {
				if (perm.getValue() != null) {
					permissionsMap.put(perm.getPropertyId(), perm.getValue());
                }
			}
		}

		map.put(WIDGET_ID, widgetItemInStep.getWidgetItem().getWidgetId());
        map.put(PRIORITY, priority);

		if (!propertiesMap.isEmpty())
			map.put(PROPERTIES, propertiesMap);
		if (!permissionsMap.isEmpty())
			map.put(PERMISSIONS, permissionsMap);

		if (tree.hasChildren(node)) {
			map.put(CHILDREN, CollectionUtils.collect(tree.getChildren(node), new Transformer() {
                private Integer priorityCounter = 1;
                
				@Override
				public Object transform(Object node) {
                    priorityCounter++;
					return collectNode(tree, node, priorityCounter);
				}
			}));
		}

		return map;
	}

	protected static String dumpTreeToJSON(Tree tree, WidgetItemInStep rootItem, Object assignee, 
                                           Object candidateGroups, Object swimlane, String stepName, 
                                           List<Permission> permissions) {
		TaskConfig tc = new TaskConfig();
		tc.setTaskName(stepName);
		
		Map<String, Object> treeMap = collectNode(tree, rootItem, 1);
		treeMap.put(ASSIGNEE, assignee);
		treeMap.put(CANDIDATE_GROUPS, candidateGroups);
		treeMap.put(SWIMLANE, swimlane);
        treeMap.put("step-permissions", permissions);
		
        tc.setParams(treeMap);
        
		try {
            String s = mapper.writeValueAsString(tc);
            logger.info("Dumped result: " +s);
            return s;
		} catch (JsonGenerationException e) {
            logger.log(Level.SEVERE, "Error dumping tree", e);
		} catch (JsonMappingException e) {
            logger.log(Level.SEVERE, "Error dumping tree", e);
		} catch (IOException e) {
            logger.log(Level.SEVERE, "Error dumping tree", e);
		}
		return Messages.getString("dump.failed");
	}

}
