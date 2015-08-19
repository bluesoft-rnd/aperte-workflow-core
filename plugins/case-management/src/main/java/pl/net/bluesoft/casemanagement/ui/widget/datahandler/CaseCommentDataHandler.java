package pl.net.bluesoft.casemanagement.ui.widget.datahandler;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import pl.net.bluesoft.casemanagement.model.Case;
import pl.net.bluesoft.casemanagement.model.CaseAttributes;
import pl.net.bluesoft.casemanagement.model.CaseComment;
import pl.net.bluesoft.casemanagement.model.CaseCommentsAttribute;
import pl.net.bluesoft.rnd.processtool.model.IAttributesConsumer;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetDataEntry;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static pl.net.bluesoft.casemanagement.model.util.CaseModelUtil.getCaseComments;

/**
 * Created by pkuciapski on 2014-05-15.
 */
public class CaseCommentDataHandler implements IWidgetDataHandler {
    private static final String TYPE_COMMENT = "comment";

    @Override
	public void handleWidgetData(IAttributesConsumer consumer, WidgetData data) {
        ObjectMapper mapper = new ObjectMapper();
        final Case caseInstance = (Case) consumer;
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, CaseCommentBean.class);

        for (WidgetDataEntry commentData : data.getEntriesByType(TYPE_COMMENT)) {
            String commentsJSON = commentData.getValue();
            try {
                List<CaseCommentBean> list = mapper.readValue(commentsJSON, type);
                List<CaseComment> comments = convert(list, caseInstance);
                CaseCommentsAttribute attr = getCaseComments(caseInstance);
                if (attr == null) {
                    attr = new CaseCommentsAttribute();
                    caseInstance.setAttribute(CaseAttributes.COMMENTS.value(), attr);
                }
                attr.getComments().addAll(comments);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

	private List<CaseComment> convert(List<CaseCommentBean> list, Case caseInstance) {
        List<CaseComment> result = new ArrayList<CaseComment>();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        for (CaseCommentBean bean : list) {
            result.add(convert(bean, caseInstance, format));
        }
        return result;
    }

    private CaseComment convert(CaseCommentBean bean, Case caseInstance, SimpleDateFormat format) {
        CaseComment comment = new CaseComment();
        comment.setAuthorLogin(bean.getAuthorLogin());
        comment.setAuthorFullName(bean.getAuthorFullName());
        comment.setBody(bean.getBody());
        try {
            comment.setCreateDate(format.parse(bean.getCreateDate()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return comment;
    }
}
