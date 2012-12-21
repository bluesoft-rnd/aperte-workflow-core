package pl.net.bluesoft.rnd.processtool.ui.widgets.event;

import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2012-08-23
 * Time: 14:45
 */
public class WidgetEventBus {
	private List<ProcessToolWidget> widgets = new ArrayList<ProcessToolWidget>();

	public void subscribe(ProcessToolWidget widget) {
		if (!widgets.contains(widget)) {
			widgets.add(widget);
		}
	}

	public void unsubscribe(ProcessToolWidget widget) {
		widgets.remove(widget);
	}

	public void broadcast(WidgetEvent event) {
		for (ProcessToolWidget widget : new ArrayList<ProcessToolWidget>(widgets)) {
			widget.handleWidgetEvent(event);
		}
	}
}
