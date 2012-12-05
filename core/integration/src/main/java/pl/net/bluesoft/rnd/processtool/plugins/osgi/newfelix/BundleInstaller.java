package pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix;

import java.util.logging.Logger;

/**
 * User: POlszewski
 * Date: 2012-11-29
 * Time: 13:14
 */
public abstract class BundleInstaller {
	private NewFelixBundleService felixService;
	private BundleInfo bundleInfo;
	private Logger logger;

	public BundleInstaller(NewFelixBundleService felixService, BundleInfo bundleInfo, Logger logger) {
		this.felixService = felixService;
		this.bundleInfo = bundleInfo;
		this.logger = logger;
	}

	public abstract void syncBundlesWithRepository();

	public NewFelixBundleService getFelixService() {
		return felixService;
	}

	public BundleInfo getBundleInfo() {
		return bundleInfo;
	}

	public Logger getLogger() {
		return logger;
	}
}
