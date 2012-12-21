package org.aperteworkflow.editor.stepeditor.user;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.ui.Tree;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.aperteworkflow.editor.domain.Permission;
import org.aperteworkflow.editor.stepeditor.TaskConfig;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.io.IOException;
import java.util.*;
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
    public static final String DESCRIPTION = "description";
    public static final String COMMENTARY = "commentary";

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
                                                 String config, Collection<Permission> permissions) throws WidgetNotFoundException, ParsingFailedException {
		try {
			Map<String, Object> map = mapper.readValue(config, Map.class);

			analyzeChildren(map, hc, rootItem);
			
			HashMap<String, String> resultMap = new HashMap<String, String>();
			if(map.get(ASSIGNEE) != null){
				resultMap.put(ASSIGNEE, map.get(ASSIGNEE).toString());
			}
			if(map.get(SWIMLANE) != null){
				resultMap.put(SWIMLANE, map.get(SWIMLANE).toString());
			}
			if(map.get(CANDIDATE_GROUPS) != null){
				resultMap.put(CANDIDATE_GROUPS, map.get(CANDIDATE_GROUPS).toString());
			}
            if (map.get(DESCRIPTION) != null) {
                resultMap.put(DESCRIPTION, map.get(DESCRIPTION).toString());
            }
            if (map.get(COMMENTARY) != null) {
                resultMap.put(COMMENTARY, decodeAndCreateString( map.get(COMMENTARY).toString()));
            }
            
            if (map.containsKey(STEP_PERMISSIONS)) {
                Collection<Map> jsonPermissions = (Collection<Map>) map.get(STEP_PERMISSIONS);
                for (Map m : jsonPermissions) {
                    permissions.add(parsePrivilege(m));
                }
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
	
	public static String decodeAndCreateString(String stringToDecode){
		
		return new String(Base64.decodeBase64(stringToDecode.getBytes()));
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
			for (Map.Entry<String, Object> e : properties.entrySet()) {
				for (Property<?> property : item.getProperties()) {
					if (property.getPropertyId().equals(e.getKey())) {
						property.setValue(new String(Base64.decodeBase64(e.getValue().toString())));
					}
				}
			}
		}

		try {
            Collection<Map> jsonPermissions = (Collection<Map>) node.get(PERMISSIONS);
            if (jsonPermissions != null) {
                List<Permission> permissions = new ArrayList<Permission>();
                for (Map m : jsonPermissions) {
                    permissions.add(parsePrivilege(m));
                }
                item.setPermissions(new ArrayList<Permission>(new LinkedHashSet<Permission>(permissions)));
            }
        } catch (ClassCastException e) { //TODO removeme, I exist only for backwards dev compatibility
            //nothing
        }

		
		Item newItem = hc.addItem(item);
		newItem.getItemProperty(NAME).setValue(widgetItem.getName());
		hc.setParent(item, rootItem);
		hc.setChildrenAllowed(item, widgetItem.getChildrenAllowed());

		return item;
	}

    private static Permission parsePrivilege(Map m) {
        Permission p = new Permission();
        p.setPrivilegeName((String) m.get("privilegeName"));
        p.setRoleName((String) m.get("roleName"));
        return p;
    }

    private static Map<String, Object> collectNode(final Tree tree, Object node, Integer priority) {
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

		map.put(WIDGET_ID, widgetItemInStep.getWidgetItem().getWidgetId());
        map.put(PRIORITY, priority);

		if (!propertiesMap.isEmpty()) {
			map.put(PROPERTIES, propertiesMap);
        }
		if (!widgetItemInStep.getPermissions().isEmpty()) {
			map.put(PERMISSIONS, widgetItemInStep.getPermissions());
        }

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
                                           Object description, Object commentary,
                                           Collection<Permission> permissions) {
		I18NSource messages = I18NSource.ThreadUtil.getThreadI18nSource();
		TaskConfig tc = new TaskConfig();
		tc.setTaskName(stepName);
		
		Map<String, Object> treeMap = collectNode(tree, rootItem, 1);

        if (assignee != null) {
		    treeMap.put(ASSIGNEE, assignee);
        }
        if (candidateGroups != null) {
		    treeMap.put(CANDIDATE_GROUPS, candidateGroups);
        }
        if (swimlane != null) {
	    	treeMap.put(SWIMLANE, swimlane);
        }
        if (permissions != null) {
            treeMap.put(STEP_PERMISSIONS, permissions);
        }
        if (description != null) {
            treeMap.put(DESCRIPTION,description);
        }
        if (commentary != null) {
            
            treeMap.put(COMMENTARY, encodeString(commentary));
        }
		
        tc.setParams(treeMap);
        
		try {
            return mapper.writeValueAsString(tc);
		} catch (JsonGenerationException e) {
            logger.log(Level.SEVERE, "Error dumping tree", e);
		} catch (JsonMappingException e) {
            logger.log(Level.SEVERE, "Error dumping tree", e);
		} catch (IOException e) {
            logger.log(Level.SEVERE, "Error dumping tree", e);
		}
		return messages.getMessage("dump.failed");
	}
	
	
	public static String encodeString(Object objectToConvert){
		byte[] bytes = objectToConvert.toString().getBytes();
		return Base64.encodeBase64URLSafeString(bytes);
		
		
	}

}
