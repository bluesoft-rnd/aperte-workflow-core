package pl.net.bluesoft.rnd.processtool.ui.widgets;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface WidgetRegistryInterface {
	void registerWidget(String name, Class<? extends ProcessToolWidget> cls);
}
