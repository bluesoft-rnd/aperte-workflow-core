package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 09:04
 */
public abstract class DialogWindow extends Window 
{
	private static final String DIALOG_BUTTONS_WRAPPER = "dialog-buttons-wrapper";
	
	protected I18NSource i18NSource;
	private boolean layoutBuilt = false;

	public DialogWindow() {
	}

	public I18NSource getI18NSource() {
		return i18NSource;
	}

	public void setI18NSource(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
	}

	public void buildLayout() {
		if (!layoutBuilt) {
			setCaption(getTitle());
			setModal(true);
			center();
			setContent(attachActionButtons(createContent()));
			layoutBuilt = true;
		}
	}

	protected abstract String getTitle();
	protected abstract AbstractLayout createContent();
	protected abstract Button[] createActionButtons();

	protected CssLayout wrapButtons(Button... buttons) 
	{
		CssLayout hl = new CssLayout();
		hl.addStyleName(DIALOG_BUTTONS_WRAPPER);
		hl.setMargin(true);
		hl.setWidth(100, UNITS_PERCENTAGE);
		for (Button button : buttons) {
			hl.addComponent(button);
		}
		return hl;
	}

	protected Button createActionButton(String caption) {
		return VaadinUtility.button(caption, null, "default", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                closeWindow();
            }
        });
	}

	protected <LayoutType extends AbstractLayout> LayoutType attachActionButtons(LayoutType layout) {
		Component buttons = wrapButtons(createActionButtons());
		layout.addComponent(buttons);
		return layout;
	}

	protected void closeWindow() {
		getApplication().getMainWindow().removeWindow(this);
	}

	protected String getMessage(String key) {
		return i18NSource.getMessage(key);
	}
}
