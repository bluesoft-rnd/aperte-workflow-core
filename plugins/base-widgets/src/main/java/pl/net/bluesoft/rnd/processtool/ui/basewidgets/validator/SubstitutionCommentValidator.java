package pl.net.bluesoft.rnd.processtool.ui.basewidgets.validator;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetValidator;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetDataEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * UserSubstitutionComment validator
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class SubstitutionCommentValidator implements IWidgetValidator 
{
	private static final String USER_SUBSTITUTION_COMMENT_NAME = "substitutionRequestComment";

	@Override
	public Collection<String> validate(IAttributesProvider task, WidgetData data)
	{
		Collection<String> errors = new ArrayList<String>();
		 
		WidgetDataEntry entry = data.getEntryByKey(USER_SUBSTITUTION_COMMENT_NAME);
        String comment = entry.getValue();
		
		if(comment == null || comment.isEmpty())
			errors.add("usersubstitution.widget.comment.warning");
		
		return errors;
	}

}
