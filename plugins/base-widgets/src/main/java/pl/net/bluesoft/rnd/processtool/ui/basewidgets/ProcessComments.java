package pl.net.bluesoft.rnd.processtool.ui.basewidgets;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.processdata.ProcessComment;
import pl.net.bluesoft.rnd.processtool.plugins.IBundleResourceProvider;
import pl.net.bluesoft.rnd.processtool.ui.widgets.IWidgetDataHandler;
import pl.net.bluesoft.rnd.processtool.ui.widgets.ProcessHtmlWidget;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.web.widgets.impl.FileWidgetContentProvider;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2013-10-14
 * Time: 14:20
 */
@AliasName(name = "ProcessComments")
public class ProcessComments extends ProcessHtmlWidget {
	public ProcessComments(IBundleResourceProvider bundleResourceProvider) {
		setContentProvider(new FileWidgetContentProvider("process-comments.html", bundleResourceProvider));
		setDataHandler(new DataHandler());
	}

	private static class DataHandler implements IWidgetDataHandler {
		@Override
		public void handleWidgetData(BpmTask task, Map<String, String> data) {
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ProcessCommentBean.class);

			String commentsJSON = data.get("processCommentsAddedComments");
			try {
				List<ProcessCommentBean> list = mapper.readValue(commentsJSON, type);
				List<ProcessComment> comments = convert(list);

				System.out.println(commentsJSON);
			}
			catch (IOException e) {
				e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			// TODO
		}

		private List<ProcessComment> convert(List<ProcessCommentBean> list) {
			List<ProcessComment> result = new ArrayList<ProcessComment>();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			for (ProcessCommentBean bean : list) {
				ProcessComment comment = convert(format, bean);
				result.add(comment);
			}
			return result;
		}

		private ProcessComment convert(SimpleDateFormat format, ProcessCommentBean bean) {
			ProcessComment comment = new ProcessComment();
			comment.setAuthor(bean.getAuthor());
			comment.setBody(bean.getBody());
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
		private String author;
		private String body;

		public String getCreateDate() {
			return createDate;
		}

		public void setCreateDate(String createDate) {
			this.createDate = createDate;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}
	}
}
