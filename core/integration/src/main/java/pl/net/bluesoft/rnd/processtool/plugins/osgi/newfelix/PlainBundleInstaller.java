package pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix;

import org.osgi.framework.Bundle;

import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 19:57
 */
class PlainBundleInstaller extends BundleInstaller {
	private Set<String> bundlePathsToRemove;
	private Set<String> bundlePathsToInstall;

	public PlainBundleInstaller(NewFelixBundleService felixService, BundleInfo bundleInfo, Logger logger) {
		super(felixService, bundleInfo, logger);
	}

	public void syncBundlesWithRepository() {
		getBundleInfo().installationStarted();
		getBundleInfo().updateBundleRepositoryStatus();

		determineAffectedBundlePaths();
		removeBundles();

		if (!bundlePathsToInstall.isEmpty()) {
			installBundles();
		}

		getBundleInfo().installationFinished();
	}

	/**
	 * Attempts to install remaining bundles in each iteration until nothing has been installed
	 */
	private void installBundles() {
		List<String> installableBundlePaths = from(bundlePathsToInstall).ordered().toList();
		boolean installed = true;

		while (!installableBundlePaths.isEmpty() && installed) {
			installed = false;
			for (Iterator<String> it = installableBundlePaths.iterator(); it.hasNext(); ) {
				String path = it.next();
				if (getFelixService().doInstallBundle(path)) {
					it.remove();
					installed = true;
				}
			}
		}
	}

	/**
	 * Attempts to remove given bundles. Failure in any removal does not affect other removals.
	 */
	private void removeBundles() {
		for (Bundle bundle : getFelixService().getInstalledBundles()) {
			String path = getFelixService().getBundlePath(bundle);
			if (bundlePathsToRemove.contains(path)) {
				getFelixService().doUninstallBundle(bundle);
			}
		}
	}

	private void determineAffectedBundlePaths() {
		List<String> installedBundlePaths = getFelixService().getInstalledBundlePaths();
		Collection<String> uninstalled = from(installedBundlePaths).except(getBundleInfo().getExistingBundlePaths());
		Collection<String> toReinstall = from(installedBundlePaths).intersect(getBundleInfo().getInstallableBundlePaths());
		bundlePathsToRemove = from(uninstalled).union(toReinstall).toSet();
		bundlePathsToInstall = from(getBundleInfo().getInstallableBundlePaths()).toSet();
	}
}
