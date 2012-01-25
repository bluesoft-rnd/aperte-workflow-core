package pl.net.bluesoft.rnd.processtool.ui.buttons;

import com.vaadin.Application;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolActionCallback;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolDataWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessToolVaadinActionButton;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.vaadin.VaadinUtility;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

/**
 * @author tlipski@bluesoft.net.pl
 */

@AliasName(name = "Default")
public class StandardValidatingButton implements ProcessToolVaadinActionButton {

	protected Logger logger = Logger.getLogger(StandardValidatingButton.class.getName());

	@AutoWiredProperty
	protected String label;

	@AutoWiredProperty
	protected String description;

	@AutoWiredProperty
	protected Boolean skipSaving = false;

	@AutoWiredProperty
	protected Boolean autoHide = false;

	@AutoWiredProperty
	protected String bpmAction;
	protected ProcessStateAction definition;
	protected Application application;
	protected I18NSource i18NSource;
	protected UserData loggedUser;

	@Override
	public void onButtonPress(final ProcessInstance processInstance,
	                          ProcessToolContext ctx,
	                          Set<ProcessToolDataWidget> dataWidgets,
	                          Map<ProcessToolDataWidget,
			                          Collection<String>> validationErrors,
	                          final ProcessToolActionCallback callback) {
		try {
			if (validationErrors.isEmpty()) {
				if(callback.saveProcessData())
					callback.performAction(definition);
			} else {
				if (!isSkipSaving()) {
					displayValidationErros(validationErrors);
				} else {
					final Window w = new Window(i18NSource.getMessage("process.action.validation.skip.save"));
					w.setModal(true);
					VerticalLayout vl = new VerticalLayout();
					vl.setSpacing(true);
					vl.setMargin(true);
					vl.addComponent(new Label(i18NSource.getMessage("process.action.validation.skip.save.description"), Label.CONTENT_XHTML));
					vl.addComponent(new Label(VaadinUtility.widgetsErrorMessage(i18NSource, validationErrors),
                            Label.CONTENT_XHTML));
					vl.addComponent(new Label(i18NSource.getMessage("process.action.validation.skip.save.continue"), Label.CONTENT_XHTML));

					HorizontalLayout hl = new HorizontalLayout();

					hl.setSpacing(true);
					hl.setMargin(true);
					Button buttonYes = new Button(i18NSource.getMessage("process.action.yes"));
                    buttonYes.addStyleName("default");
					hl.addComponent(buttonYes);
					buttonYes.addListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
//							if(callback.saveProcessData())
							callback.performAction(definition);
							application.getMainWindow().removeWindow(w);
						}
					});
					Button buttonNo = new Button(i18NSource.getMessage("process.action.no"));
                    buttonNo.addStyleName("default");
					buttonNo.addListener(new Button.ClickListener() {

						@Override
						public void buttonClick(Button.ClickEvent clickEvent) {
							application.getMainWindow().removeWindow(w);
						}
					});
					hl.addComponent(buttonNo);

					vl.addComponent(hl);
					vl.setComponentAlignment(hl, Alignment.BOTTOM_CENTER);
					w.setContent(vl);
					application.getMainWindow().addWindow(w);
					w.center();
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
            VaadinUtility.validationNotification(application, i18NSource, e.getMessage());
		}

	}

	private void displayValidationErros(Map<ProcessToolDataWidget, Collection<String>> errorMap) {
		String errorMessage = VaadinUtility.widgetsErrorMessage(i18NSource, errorMap);
        VaadinUtility.validationNotification(application, i18NSource, errorMessage);
	}

	public boolean isVisible(ProcessInstance processInstance) {
		return true;
	}

	public boolean isEnabled(ProcessInstance processInstance) {
		return true;
	}

	public void changeButton(Button button) {
		//nothing
	}

	@Override
	public String getLabel(ProcessInstance processInstance) {
		return nvl(label, description);
	}

	@Override
	public String getDescription(ProcessInstance processInstance) {
		return nvl(description, label);
	}

	public void setAutoHide(boolean autoHide) {
		this.autoHide = autoHide;
	}

	@Override
	public boolean isAutoHide() {
		return autoHide;
	}

	@Override
	public void setDefinition(ProcessStateAction psa) {

		this.definition = psa;
	}

	@Override
	public void saveData(ProcessInstance pi) {
		//nothing
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSkipSaving() {
		return skipSaving;
	}

	public void setSkipSaving(boolean skipSaving) {
		this.skipSaving = skipSaving;
	}

	public String getBpmAction() {
		return bpmAction;
	}

	public void setBpmAction(String bpmAction) {
		this.bpmAction = bpmAction;
	}

	@Override
	public void setApplication(Application application) {
		this.application = application;
	}

	@Override
	public void setI18NSource(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
	}

	public UserData getLoggedUser() {
		return loggedUser;
	}

	public void setLoggedUser(UserData loggedUser) {
		this.loggedUser = loggedUser;
	}
}
