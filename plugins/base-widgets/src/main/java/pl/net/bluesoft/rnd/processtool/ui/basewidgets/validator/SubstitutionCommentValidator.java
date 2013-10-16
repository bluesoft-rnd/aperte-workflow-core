package pl.net.bluesoft.rnd.processtool.ui.basewidgets.validator;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetValidator;

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
	public Collection<String> validate(BpmTask task, Map<String, String> data) 
	{
		Collection<String> errors = new ArrayList<String>();
		 
		String comment = data.get(USER_SUBSTITUTION_COMMENT_NAME);
		
		if(comment == null || comment.isEmpty())
			errors.add("usersubstitution.widget.comment.warning");
		
		return errors;
	}

}
