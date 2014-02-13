package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.authorization.IAuthorizationService;
import pl.net.bluesoft.rnd.processtool.bpm.BpmTaskNotification;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.util.lang.Strings;
import pl.net.bluesoft.util.lang.cquery.func.F;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;
import org.apache.commons.codec.binary.Base64;
import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.Formats.nvl;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2014-01-26
 */
public class NotificationServlet extends HttpServlet {
	private static final Logger logger = Logger.getLogger(NotificationServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");

		String lang = req.getParameter("l");
		String login = req.getParameter("u");
		long timestamp = Long.parseLong(nvl(req.getParameter("t"), "-1"));

		if (!Strings.hasText(lang) || !Strings.hasText(login) || timestamp < 0) {
			output(resp, "Incomplete arguments");
			return;
		}

		String[] credentials = getCredentials(req);

		if (credentials == null) {
			output(resp, "No auth");
			return;
		}

		IAuthorizationService authorizationService = ObjectFactory.create(IAuthorizationService.class);

		try {
			authorizationService.authenticateByLogin(credentials[0], credentials[1]);
		}
		catch (Exception e) {
			output(resp, "Auth failed ");
			logger.warning(String.format("Invalid login %s %s", credentials[0], credentials[1]));
			return;
		}

		Date date = timestamp != 0 ? new Date(timestamp) : null;

		doExecute(lang, login, date, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}

	private String[] getCredentials(HttpServletRequest req) {
		String authHeader = req.getHeader("Authorization");

		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);

			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.decodeBase64(st.nextToken()), "UTF-8");

						int p = credentials.indexOf(':');

						if (p != -1) {
							String login = credentials.substring(0, p).trim();
							String password = credentials.substring(p + 1).trim();

							return new String[] { login, password };
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Couldn't retrieve authentication", e);
					}
				}
			}
		}

		return null;
	}

	private static void doExecute(final String lang, final String login, final Date date, HttpServletResponse resp) throws IOException {
		List<BpmTaskNotification> notifications = getRegistry().withProcessToolContext(
				new ReturningProcessToolContextCallback<List<BpmTaskNotification>>() {
					@Override
					public List<BpmTaskNotification> processWithContext(ProcessToolContext ctx) {
						ProcessToolBpmSession session = getRegistry().getProcessToolSessionFactory().createSession(login);

						return session.getNotifications(date, getLocale(lang));
					}
				}, ProcessToolContextFactory.ExecutionType.NO_TRANSACTION);

		printXml(notifications, resp, date);
	}

	private static void printXml(List<BpmTaskNotification> notifications, HttpServletResponse resp, Date date) throws IOException {
		Date maxDate = notifications.isEmpty() ? date : getMaxDate(notifications);

		ServletOutputStream out = resp.getOutputStream();

		out.print(String.format("<r t=\"%s\">", maxDate != null ? maxDate.getTime() : 0));

		for (BpmTaskNotification notification : notifications) {
			if (notification.getCompletionDate() == null) {
				out.print(String.format("<n i=\"%s\" l=\"%s\" d=\"%s\" ad=\"%s\"/>",
						notification.getTaskId(),
						escapeXml(notification.getLink()),
						escapeXml(nvl(notification.getDescription())),
						escapeXml(nvl(notification.getAdditionalDescription()))
				));
			}
		}

		for (BpmTaskNotification notification : notifications) {
			if (notification.getCompletionDate() != null) {
				out.print(String.format("<c i=\"%s\"/>", notification.getTaskId()));
			}
		}

		out.print("</r>");

		out.close();
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

	private static void output(HttpServletResponse resp, String msg) throws IOException {
		PrintWriter out = resp.getWriter();
		out.print(msg);
		out.close();
	}
}
