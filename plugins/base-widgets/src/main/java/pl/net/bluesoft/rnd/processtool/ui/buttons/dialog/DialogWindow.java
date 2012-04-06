package pl.net.bluesoft.rnd.processtool.ui.buttons.dialog;

import com.vaadin.ui.*;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-02-20
 * Time: 09:04
 */
public abstract class DialogWindow extends Window {
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
	protected abstract AbstractOrderedLayout createContent();
	protected abstract Button[] createActionButtons();

	protected HorizontalLayout wrapButtons(Button... buttons) {
		HorizontalLayout hl = new HorizontalLayout();
		hl.setSpacing(true);
		hl.setMargin(true);
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

	protected <LayoutType extends AbstractOrderedLayout> LayoutType attachActionButtons(LayoutType layout) {
		Component buttons = wrapButtons(createActionButtons());
		layout.addComponent(buttons);
		layout.setComponentAlignment(buttons, Alignment.BOTTOM_CENTER);
		return layout;
	}

	protected void closeWindow() {
		getApplication().getMainWindow().removeWindow(this);
	}

	protected String getMessage(String key) {
		return i18NSource.getMessage(key);
	}
}
