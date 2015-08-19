package pl.net.bluesoft.rnd.processtool.ui.widgets;

import com.vaadin.ui.RichTextArea;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AperteDoc;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredPropertyConfigurator;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.BaseProcessToolWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.impl.MockWidgetValidator;
import pl.net.bluesoft.rnd.processtool.web.domain.IContentProvider;
import pl.net.bluesoft.rnd.util.AnnotationUtil;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Widget in-memory model
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ProcessHtmlWidget extends BaseProcessToolWidget
{
	private String widgetName;
	private Collection<IWidgetDataHandler> dataHandlers = new LinkedList<IWidgetDataHandler>();
    private Collection<IWidgetDataProvider> dataProviders = new LinkedList<IWidgetDataProvider>();
	private IWidgetValidator validator = new MockWidgetValidator();
	private IContentProvider contentProvider;

    /**
     * Widget caption text
     */
    @AutoWiredProperty
    @AperteDoc(
            humanNameKey = "widget.attribute.caption.humanName",
            descriptionKey = "widget.attribute.caption.description"
    )
    protected String caption;

    /**
     * Widget comment text
     */
    @AutoWiredProperty
    @AutoWiredPropertyConfigurator(fieldClass = RichTextArea.class)
    @AperteDoc(
            humanNameKey = "widget.attribute.comment.humanName",
            descriptionKey = "widget.attribute.comment.description"
    )
    protected String comment;

	protected ProcessHtmlWidget()
    {
		this.widgetName = AnnotationUtil.getAliasName(getClass());
	}

	protected ProcessHtmlWidget(String widgetName) {
		this.widgetName = widgetName;
	}

    public void addDataHandler(IWidgetDataHandler dataHandler)
    {
        this.dataHandlers.add(dataHandler);
    }

    public void addDataProvider(IWidgetDataProvider widgetDataProvider)
    {
        this.dataProviders.add(widgetDataProvider);
    }

    public Collection<IWidgetDataProvider> getDataProviders() {
        return dataProviders;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
	
	public String getWidgetName() {
		return widgetName;
	}
	public void setWidgetName(String widgetName) {
		this.widgetName = widgetName;
	}

    public Collection<IWidgetDataHandler> getDataHandlers() {
        return dataHandlers;
    }

    public void setDataHandlers(Collection<IWidgetDataHandler> dataHandlers) {
        this.dataHandlers = dataHandlers;
    }

    public IWidgetValidator getValidator() {
		return validator;
	}
	public void setValidator(IWidgetValidator validator) {
		this.validator = validator;
	}
	public IContentProvider getContentProvider() {
		return contentProvider;
	}
	public void setContentProvider(IContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((widgetName == null) ? 0 : widgetName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessHtmlWidget other = (ProcessHtmlWidget) obj;
		if (widgetName == null) {
			if (other.widgetName != null)
				return false;
		} else if (!widgetName.equals(other.widgetName))
			return false;
		return true;
	}


    @Override
    public void addChild(ProcessToolWidget child) {
    }

    public boolean hasContnet()
    {
        return contentProvider != null;
    }

	public void getViewData(Map<String, Object> viewData) {
	}
}
