package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolChildrenFilteringWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinRenderable;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.ChildrenAllowed;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.WidgetGroup;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolVaadinWidget;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

@AliasName(name = "SwitchWidgets")
@AperteDoc(humanNameKey = "widget.switch_widget.name", descriptionKey = "widget.switch_widget.description")
@ChildrenAllowed(true)
@WidgetGroup("base-widgets")
public class SwitchWidget extends BaseProcessToolVaadinWidget implements ProcessToolVaadinRenderable,
		ProcessToolChildrenFilteringWidget {
	private static final Logger			logger	= Logger.getLogger(SwitchWidget.class.getName());

	@AutoWiredProperty(required = true)
	@AperteDoc(humanNameKey = "widget.switch_widget.property.selectorKey.name", descriptionKey = "widget.switch_widget.property.selectorKey.description")
	String								selectorKey;

	@AutoWiredProperty(required = true)
	@AperteDoc(humanNameKey = "widget.switch_widget.property.conditions.name", descriptionKey = "widget.switch_widget.property.conditions.description")
	String								conditions;

	List<ProcessToolVaadinRenderable>	widgets	= new ArrayList<ProcessToolVaadinRenderable>();

	public SwitchWidget() {
	}

	@Override
	public Component render() {
		if (widgets.size() > 0)
			return widgets.get(0).render();
		else {
			return null;
		}
	}
	
	public boolean hasWidgets() {
		return widgets.size() > 0;
	}

	@Override
	public void addChild(ProcessToolWidget child) {
		if (!(child instanceof ProcessToolVaadinRenderable)) {
			throw new IllegalArgumentException("child is not instance of "
					+ ProcessToolVaadinRenderable.class.getName());
		}
		ProcessToolVaadinRenderable vChild = (ProcessToolVaadinRenderable) child;
		widgets.add(vChild);
	}

	@Override
	public List<ProcessStateWidget> filterChildren(BpmTask task, List<ProcessStateWidget> sortedList) {
		String key = task.getProcessInstance().getSimpleAttributeValue(selectorKey);
		if (key == null) {
			key = task.getProcessInstance().getRootProcessInstance().getSimpleAttributeValue(selectorKey);
		}
		if(key == null){
	    	Map<String, Object> variables = ProcessToolContext.Util.getThreadProcessToolContext().getBpmVariables(task.getProcessInstance());
	    	key = (String) variables.get(selectorKey);
		}
		
		if(key == null)
			return new ArrayList<ProcessStateWidget>(0);
		
		String[] conditionsArray = conditions.split("[,; ]+");
		int index = -1;
		for (int i = 0; i < conditionsArray.length; i++) {
			if (key.equals(conditionsArray[i].trim()))
				index = i;
		}
		try {
			return Arrays.asList(sortedList.get(index));
		} catch (IndexOutOfBoundsException e) {
			return new ArrayList<ProcessStateWidget>(0);
		}
	}
}
