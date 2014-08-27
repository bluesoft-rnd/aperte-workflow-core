package pl.net.bluesoft.interactivereports.templates;

import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Map;

/**
 * User: POlszewski
 * Date: 2014-06-24
 */
public interface InteractiveReportTemplate {
	String getName();

	class RenderParams {
		private UserData user;
		private I18NSource messageSource;
		private Map<String, Object> reportParams;

		public UserData getUser() {
			return user;
		}

		public void setUser(UserData user) {
			this.user = user;
		}

		public I18NSource getMessageSource() {
			return messageSource;
		}

		public void setMessageSource(I18NSource messageSource) {
			this.messageSource = messageSource;
		}

		public Map<String, Object> getReportParams() {
			return reportParams;
		}

		public void setReportParams(Map<String, Object> reportParams) {
			this.reportParams = reportParams;
		}

		public Object getReportParam(String name) {
			return reportParams.get(name);
		}

		public void setReportParam(String name, Object value) {
			reportParams.put(name, value);
		}
	}

	String renderReportParams(RenderParams params);
	String renderReport(RenderParams params);

	class ExportParams extends RenderParams {
		private String desiredFormat;

		public String getDesiredFormat() {
			return desiredFormat;
		}

		public void setDesiredFormat(String desiredFormat) {
			this.desiredFormat = desiredFormat;
		}
	}

	class ExportResult {
		private String fileName;
		private String contentType;
		private byte[] content;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(byte[] content) {
			this.content = content;
		}
	}

	ExportResult export(ExportParams params);

	boolean isAvailable(UserData user);
}
