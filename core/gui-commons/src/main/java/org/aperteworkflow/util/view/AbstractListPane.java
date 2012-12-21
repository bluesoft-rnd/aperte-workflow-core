package org.aperteworkflow.util.view;

import com.vaadin.Application;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.util.vaadin.ResourceCache;
import org.aperteworkflow.util.vaadin.VaadinUtility;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import static org.aperteworkflow.util.vaadin.VaadinUtility.horizontalLayout;
import static org.aperteworkflow.util.vaadin.VaadinUtility.refreshIcon;


/**
 * @author: amichalak@bluesoft.net.pl
 */
public abstract class AbstractListPane extends VerticalLayout implements VaadinUtility.Refreshable {
    protected Application application;
    protected I18NSource messageSource;

    protected String title;
    protected Label titleLabel;
	protected boolean addRefreshButton = true;

    private ResourceCache resourceCache;

    protected AbstractListPane() {

    }

    protected void setUp(Application application, I18NSource messageSource, String title) {
        this.application = application;
        this.messageSource = messageSource;
        this.title = title;
		this.resourceCache = new ResourceCache(application);
    }
    protected AbstractListPane(Application application, I18NSource messageSource, String title) {
        setUp(application, messageSource, title);
    }

    public AbstractListPane init() {
        removeAllComponents();

        setWidth(100, UNITS_PERCENTAGE);
        setMargin(true);
        setSpacing(true);

		titleLabel = new Label(getMessage(title), Label.CONTENT_XHTML) {{
			addStyleName("h1 color processtool-title");
		}};

		if (addRefreshButton) {
        	addComponent(horizontalLayout(titleLabel, refreshIcon(application, this)));
		}
		else {
			addComponent(titleLabel);
		}

        return this;
    }



    public void setTitle(String title) {
        this.title = title;
        if (titleLabel != null) {
            titleLabel.setValue(title);
        }
    }

    public String getMessage(String key) {
        return messageSource.getMessage(key);
    }

    protected void cacheResource(String path, Resource resource) {
        resourceCache.cacheResource(path, resource);
    }

    protected Resource getResource(String path) {
        return resourceCache.getResource(path);
    }

    protected Resource getImage(String path) {
        return resourceCache.getImage(path);
    }
}
