package pl.net.bluesoft.rnd.processtool.ui.basewidgets.datahandler;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.IAttributesProvider;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.ui.widgets.HandlingResult;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetData;
import pl.net.bluesoft.rnd.processtool.ui.widgets.WidgetDataEntry;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Maciej
 */
public class CommentDataHandler implements IWidgetDataHandler
{
    private static final String TYPE_COMMENT = "comment";

    public Collection<HandlingResult> handleWidgetData(IAttributesProvider provider,  WidgetData data)
    {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ProcessCommentBean.class);

        for(WidgetDataEntry commentData: data.getEntriesByType(TYPE_COMMENT)) {
            String commentsJSON = commentData.getValue();
            try {
                List<ProcessCommentBean> list = mapper.readValue(commentsJSON, type);
                List<ProcessComment> comments = convert(list, provider);
                provider.getProcessInstance().addComments(comments);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new LinkedList<HandlingResult>();
    }

    private List<ProcessComment> convert(List<ProcessCommentBean> list, IAttributesProvider provider) {
        List<ProcessComment> result = new ArrayList<ProcessComment>();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        for (ProcessCommentBean bean : list) {
            result.add(convert(bean, (BpmTask)provider, format));
        }
        return result;
    }

    private ProcessComment convert(ProcessCommentBean bean, BpmTask task, SimpleDateFormat format) {
        ProcessComment comment = new ProcessComment();
        comment.setAuthorLogin(bean.getAuthorLogin());
        comment.setAuthorFullName(bean.getAuthorFullName());
        comment.setBody(bean.getBody());
        comment.setProcessState(task.getTaskName());
        comment.setProcessInstance(task.getProcessInstance());
        try {
            comment.setCreateTime(format.parse(bean.getCreateDate()));
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return comment;
    }
}
