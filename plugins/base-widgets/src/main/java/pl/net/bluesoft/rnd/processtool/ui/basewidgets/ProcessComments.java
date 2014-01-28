package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.*;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: POlszewski
 * Date: 2013-10-14
 * Time: 14:20
 */
@AliasName(name = "ProcessComments")
public class ProcessComments extends ProcessHtmlWidget
{
    private static final String TYPE_COMMENT = "comment";

	public ProcessComments(IBundleResourceProvider bundleResourceProvider) {
		setContentProvider(new FileWidgetContentProvider("process-comments.html", bundleResourceProvider));
		setDataHandler(new DataHandler());
	}

	private static class DataHandler implements IWidgetDataHandler {
		@Override
		public Collection<HandlingResult> handleWidgetData(BpmTask task,  WidgetData data) {
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ProcessCommentBean.class);

            WidgetDataEntry commentData =  data.getEntryByKey("processCommentsAddedComments");
			String commentsJSON = commentData.getValue();
			try {
				List<ProcessCommentBean> list = mapper.readValue(commentsJSON, type);
				List<ProcessComment> comments = convert(list, task);
				task.getProcessInstance().addComments(comments);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
            return new LinkedList<HandlingResult>();
		}

		private List<ProcessComment> convert(List<ProcessCommentBean> list, BpmTask task) {
			List<ProcessComment> result = new ArrayList<ProcessComment>();
			SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

			for (ProcessCommentBean bean : list) {
				result.add(convert(bean, task, format));
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

	public static class ProcessCommentBean {
		private String createDate;
		private String authorLogin;
        private String authorFullName;
		private String body;

		public String getCreateDate() {
			return createDate;
		}

		public void setCreateDate(String createDate) {
			this.createDate = createDate;
		}

		public String getAuthorLogin() {
			return authorLogin;
		}

		public void setAuthorLogin(String authorLogin) {
			this.authorLogin = authorLogin;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

        public String getAuthorFullName() {
            return authorFullName;
        }

        public void setAuthorFullName(String authorFullName) {
            this.authorFullName = authorFullName;
        }
    }
}
