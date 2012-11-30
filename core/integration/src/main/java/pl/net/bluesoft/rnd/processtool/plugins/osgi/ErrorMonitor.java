package pl.net.bluesoft.rnd.processtool.plugins.osgi;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 16:26
 */
public class ErrorMonitor {
	private final StringBuffer monitorInfo = new StringBuffer();

	public void forwardErrorInfoToMonitor(String path, Exception e) {
		monitorInfo.append("\nSEVERE EXCEPTION: ")
				.append(path)
				.append("\n")
				.append(e.getMessage());
	}

	public String getMonitorInfo() {
		return monitorInfo.toString();
	}
}
