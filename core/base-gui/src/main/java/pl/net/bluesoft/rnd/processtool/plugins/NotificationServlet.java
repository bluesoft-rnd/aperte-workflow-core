package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTaskNotification;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.cquery.func.F;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2014-01-26
 */
public class NotificationServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");

		String lang = req.getParameter("l");
		String login = req.getParameter("u");
		long timestamp = Long.parseLong(nvl(req.getParameter("t"), "-1"));

		if (!Strings.hasText(lang) || !Strings.hasText(login) || timestamp < 0) {
			return;
		}

		Date date = timestamp != 0 ? new Date(timestamp) : null;

		PrintWriter out = resp.getWriter();

		try {
			doExecute(lang, login, date, out);
		}
		finally {
			out.close();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	private static void doExecute(final String lang, final String login, final Date date, PrintWriter out) {
		List<BpmTaskNotification> notifications = getRegistry().withProcessToolContext(
				new ReturningProcessToolContextCallback<List<BpmTaskNotification>>() {
					@Override
					public List<BpmTaskNotification> processWithContext(ProcessToolContext ctx) {
						ProcessToolBpmSession session = getRegistry().getProcessToolSessionFactory().createSession(login);

						return session.getNotifications(date, getLocale(lang));
					}
				}, ProcessToolContextFactory.ExecutionType.NO_TRANSACTION);

		printXml(notifications, out, date);
	}

	private static void printXml(List<BpmTaskNotification> notifications, PrintWriter out, Date date) {
		Date maxDate = notifications.isEmpty() ? date : getMaxDate(notifications);

		out.print(String.format("<r t=\"%s\">", maxDate != null ? maxDate.getTime() : 0));

		for (BpmTaskNotification notification : notifications) {
			if (notification.getCompletionDate() == null) {
				out.print(String.format("<n i=\"%s\" l=\"%s\" d=\"%s\"/>",
						notification.getTaskId(),
						escapeXml(notification.getLink()),
						escapeXml(notification.getDescription())
				));
			}
		}

		for (BpmTaskNotification notification : notifications) {
			if (notification.getCompletionDate() != null) {
				out.print(String.format("<c i=\"%s\"/>", notification.getTaskId()));
			}
		}

		out.print("</r>");
	}

	private static Date getMaxDate(List<BpmTaskNotification> notifications) {
		return from(notifications).select(new F<BpmTaskNotification, Date>() {
			@Override
			public Date invoke(BpmTaskNotification x) {
				return nvl(x.getCompletionDate(), x.getCreationDate());
			}
		}).max();
	}

	private static Locale getLocale(String lang) {
		String[] parts = lang.split("_");
		return parts.length > 1 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
	}
}
