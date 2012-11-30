package pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;
import static pl.net.bluesoft.util.lang.cquery.CQuery.repeat;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 16:59
 */
class BundleInfo {
	public enum Status {
		UNKNOWN("???"),
		INSTALLATION_STARTED("INSTALLING"),
		INSTALLATION_FAILED("INSTALL ERROR"),
		INSTALLATION_SUCCEEDED("INSTALLED"),
		UNINSTALLATION_STARTED("UNINSTALLING"),
		UNINSTALLATION_FAILED("UNINSTALL ERROR"),
		UNINSTALLATION_SUCCEEDED("UNINSTALLED");

		private final String description;

		Status(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}
	}

	private static class BundleInfoEntry {
		private long lastModified;
		private long installDuration;
		private final Set<String> exportedPackages = new HashSet<String>();
		private final Set<String> importedPackages = new HashSet<String>();
		private long installationStart;
		private Status status = Status.UNKNOWN;

		public long getLastModified() {
			return lastModified;
		}

		public void setLastModified(long lastModified) {
			this.lastModified = lastModified;
		}

		public long getInstallDuration() {
			return installDuration;
		}

		public Set<String> getExportedPackages() {
			return exportedPackages;
		}

		public Set<String> getImportedPackages() {
			return importedPackages;
		}

		public void setStatus(Status status) {
			this.status = status;
			if (status == Status.INSTALLATION_STARTED || status == Status.UNINSTALLATION_STARTED) {
				installationStart = System.currentTimeMillis();
			}
			else {
				installDuration += System.currentTimeMillis() - installationStart;
			}
		}

		public Status getStatus() {
			return status;
		}

		public void resetInstallationStatistics() {
			installationStart = 0;
			installDuration = 0;
		}

		public boolean hasBeenModified() {
			return installationStart > 0;
		}
	}

	private static final String STATUS_SEPARATOR = repeat("-", 80).toString("", "", "\n");

	private final Map<String, BundleInfoEntry> entries = new HashMap<String, BundleInfoEntry>();
	private final Set<String> systemPackages = new HashSet<String>();
	private long bundleInstallationStart;
	private long bundleInstallationTime;

	private String pluginsDir;
	private Set<String> existingBundlePaths;
	private List<String> installableBundlePaths;

	private Map<String, Set<String>> dependencyMap;
	private Map<String, Set<String>> inverseDependencyMap;

	private Logger logger;

	public BundleInfo(Logger logger) {
		this.logger = logger;
	}

	private BundleInfoEntry getEntry(String bundlePath) {
		BundleInfoEntry info = entries.get(bundlePath);
		if (info == null) {
			entries.put(bundlePath, info = new BundleInfoEntry());
		}
		return info;
	}

	public void setSystemPackages(String systemPackagesString) {
		systemPackagesString = systemPackagesString.replaceAll("\\s+", "");
		ExportParser parser = new ExportParser(systemPackagesString);
		systemPackages.clear();
		systemPackages.addAll(parser.parsePackageNamesOnly());
	}

	public boolean isSystemPackage(String importedPack) {
		return systemPackages.contains(importedPack);
	}

	private void updatePackageInfo(String bundlePath, String importPackageAttr, String exportPackageAttr) {
		ExportParser importParser = new ExportParser(importPackageAttr);
		getImportedPackages(bundlePath).addAll(importParser.parsePackageNamesOnly());
		getImportedPackages(bundlePath).remove("*");

		ExportParser exportParser = new ExportParser(exportPackageAttr);
		getExportedPackages(bundlePath).addAll(exportParser.parsePackageNamesOnly());
		getImportedPackages(bundlePath).removeAll(getExportedPackages(bundlePath));
	}

	public void updateDependencyInfo() {
		for (String installBundlePath : installableBundlePaths) {
			JarFile jar = null;
			try {
				jar = new JarFile(installBundlePath);
				Manifest manifest = jar.getManifest();
				String importPackageAttr = manifest.getMainAttributes().getValue("Import-Package");
				String exportPackageAttr = manifest.getMainAttributes().getValue("Export-Package");

				updatePackageInfo(installBundlePath, importPackageAttr, exportPackageAttr);
			}
			catch (Exception e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
			finally {
				try {
					if (jar != null) {
						jar.close();
					}
				}
				catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
		resetDependencyMap();
	}

	public Map<String, Set<String>> getDependencyMap() {
		if (dependencyMap == null) {
			computeDependencyMap();
		}
		return dependencyMap;
	}

	public Map<String, Set<String>> getInverseDependencyMap() {
		if (inverseDependencyMap == null) {
			computeDependencyMap();
		}
		return inverseDependencyMap;
	}

	private void computeDependencyMap() {
		dependencyMap = new HashMap<String, Set<String>>();

//		Set<String> errors = new HashSet<String>();

		for (String bundleName : entries.keySet()) {
			Set<String> deps = new HashSet<String>();
			dependencyMap.put(bundleName, deps);
//			boolean error = false;

			for (String importedPack : getImportedPackages(bundleName)) {
				if (isSystemPackage(importedPack)) {
					continue;
				}
//				int cnt = 0;
				for (String potentialDependency : entries.keySet()) {
					if (getExportedPackages(potentialDependency).contains(importedPack)) {
						deps.add(potentialDependency);
//						++cnt;
					}
				}
//				if (cnt == 0) {
//					error = true;
//					logger.log(Level.SEVERE, String.format("Dependency analysis: bundle %s imports unknown package %s", bundleName, importedPack));
//				}
//				else if (cnt > 1) {
//					error = true;
//					logger.log(Level.SEVERE, String.format("Dependency analysis: bundle %s imports package %s beging exported by more than 1 plugin. It may cause problems", bundleName, importedPack));
//				}
//				if (error) {
//					errors.add(bundleName);
//				}
			}
		}

		inverseDependencyMap = new HashMap<String, Set<String>>();

		for (String bundleName : entries.keySet()) {
			inverseDependencyMap.put(bundleName, new HashSet<String>());
		}

		for (Map.Entry<String, Set<String>> entry : dependencyMap.entrySet()) {
			for (String dep : entry.getValue()) {
				Set<String> invDeps = inverseDependencyMap.get(dep);
				invDeps.add(entry.getKey());
			}
		}
	}

	public void installationStarted() {
		this.bundleInstallationStart = System.currentTimeMillis();
		for (BundleInfoEntry entry : entries.values()) {
			entry.resetInstallationStatistics();
		}
	}

	public void installationFinished() {
		this.bundleInstallationTime = System.currentTimeMillis() - bundleInstallationStart;

		String installationStatus = getInstallationStatus();

		if (installationStatus != null) {
			logger.info(installationStatus);
		}
	}

	public void installationStarted(String bundlePath) {
		getEntry(bundlePath).setStatus(Status.INSTALLATION_STARTED);
	}

	public void installationFailed(String bundlePath) {
		getEntry(bundlePath).setStatus(Status.INSTALLATION_FAILED);
	}

	public void installationFinished(String bundlePath) {
		getEntry(bundlePath).setStatus(Status.INSTALLATION_SUCCEEDED);
	}

	public void uninstallationStarted(String bundlePath) {
		getEntry(bundlePath).setStatus(Status.UNINSTALLATION_STARTED);
	}

	public void uninstallationFailed(String bundlePath) {
		getEntry(bundlePath).setStatus(Status.UNINSTALLATION_FAILED);
	}

	public void uninstallationFinished(String bundlePath) {
		getEntry(bundlePath).setStatus(Status.UNINSTALLATION_SUCCEEDED);
	}

	public Collection<String> getImportedPackages(String bundlePath) {
		return getEntry(bundlePath).getImportedPackages();
	}

	public Collection<String> getExportedPackages(String bundlePath) {
		return getEntry(bundlePath).getExportedPackages();
	}

	public Long getInstallDuration(String bundlePath) {
		return getEntry(bundlePath).getInstallDuration();
	}

	public Status getBundleStatus(String bundlePath) {
		return getEntry(bundlePath).getStatus();
	}

	public String getInstallationStatus() {
		if (getAffectedBundlePaths().isEmpty()) {
			return null;
		}

		StringBuilder status = new StringBuilder();

		status.append('\n');
		status.append(STATUS_SEPARATOR);
		status.append("Bundle installation status\n");
		status.append(STATUS_SEPARATOR);

		for (String path : getAffectedBundlePaths()) {
			status.append(String.format("%-42s %-18s %8.3f seconds",
					new File(path).getName().replaceAll("\\.jar$", ""),
					getEntry(path).getStatus().getDescription(),
					nvl(getInstallDuration(path), 0L)/1000.0));
			status.append('\n');
		}

		status.append(STATUS_SEPARATOR);
		status.append(String.format("Bundles installed in %s seconds\n", bundleInstallationTime / 1000.0));

		if (!getNotInstalledPlugins().isEmpty()) {
			status.append("UNABLE TO INSTALL BUNDLES:\n").append(from(getNotInstalledPlugins()).toString("\n\t", "\t", "\n"));
		}

		status.append(STATUS_SEPARATOR);

		return status.toString();
	}

	private Collection<String> getAffectedBundlePaths() {
		List<String> bundlePaths = new ArrayList<String>();

		for (Map.Entry<String, BundleInfoEntry> entry : entries.entrySet()) {
			if (entry.getValue().hasBeenModified()) {
				bundlePaths.add(entry.getKey());
			}
		}

		return from(bundlePaths).ordered().toList();
	}

	private List<String> getNotInstalledPlugins() {
		List<String> notInstalledPlugins = new ArrayList<String>();
		for (String bundlePath : installableBundlePaths) {
			if (getEntry(bundlePath).getStatus() == Status.INSTALLATION_FAILED) {
				notInstalledPlugins.add(bundlePath);
			}
		}
		return from(notInstalledPlugins).ordered().toList();
	}

	public void updateBundleRepositoryStatus() {
		existingBundlePaths = new HashSet<String>();
		installableBundlePaths = new ArrayList<String>();

		for (File bundleFile : getExistingBundleFiles()) {
			String path = bundleFile.getAbsolutePath();
			BundleInfoEntry entry = getEntry(path);

			if (entry.getLastModified() < bundleFile.lastModified()/* || entry.getStatus() == Status.UNINSTALLATION_SUCCEEDED*/) {
				installableBundlePaths.add(path);
				entry.setLastModified(bundleFile.lastModified());
			}
			existingBundlePaths.add(path);
		}

		resetDependencyMap();
	}

	private void resetDependencyMap() {
		dependencyMap = null;
		inverseDependencyMap = null;
	}

	private List<File> getExistingBundleFiles() {
		List<File> bundleFiles = new ArrayList<File>();
		File pluginsDirFile = new File(pluginsDir);

		if (!pluginsDirFile.exists()) {
			logger.warning("Plugins dir not found: " + pluginsDir + " attempting to create...");
			if (!pluginsDirFile.mkdir()) {
				logger.severe("Failed to create plugins directory: " + pluginsDir + ", please reconfigure!!!");
				return bundleFiles;
			} else {
				logger.info("Created plugins directory: " + pluginsDir);
			}
		}

		String[] list = pluginsDirFile.list();
		Arrays.sort(list);

		for (String filename : list) {
			File bundleFile = new File(pluginsDirFile.getAbsolutePath() + File.separator + filename);
			String path = bundleFile.getAbsolutePath();

			if (!bundleFile.isDirectory() && path.matches(".*jar$")) {
				bundleFiles.add(bundleFile);
			}
		}
		return bundleFiles;
	}

	public Set<String> getExistingBundlePaths() {
		return existingBundlePaths;
	}

	public List<String> getInstallableBundlePaths() {
		return installableBundlePaths;
	}

	public void setPluginsDir(String pluginsDir) {
		this.pluginsDir = pluginsDir;
	}

	public Set<String> getDependencyClosure(Collection<String> bundlePaths) {
		if (bundlePaths.isEmpty()) {
			return Collections.emptySet();
		}
		return getDependencyClosure(getDependencyMap(), bundlePaths);
	}

	public Set<String> getInverseDependencyClosure(Collection<String> bundlePaths) {
		if (bundlePaths.isEmpty()) {
			return Collections.emptySet();
		}
		return getDependencyClosure(getInverseDependencyMap(), bundlePaths);
	}

	private static Set<String> getDependencyClosure(Map<String, Set<String>> dependencyMap, Collection<String> bundlePaths) {
		Set<String> closure = new HashSet<String>();
		Set<String> toAdd = new HashSet<String>(bundlePaths);

		while (!toAdd.isEmpty()) {
			String first = removeFirst(toAdd);
			closure.add(first);
			for (String dependency : dependencyMap.get(first)) {
				if (!closure.contains(dependency)) {
					toAdd.add(dependency);
				}
			}
		}
		return closure;
	}

	public Set<String> getBundleDependencies(String bundlePath) {
		return getDependencyMap().get(bundlePath);
	}

	public Set<String> getInverseBundleDependencies(String bundlePath) {
		return getInverseDependencyMap().get(bundlePath);
	}

	private static String removeFirst(Set<String> toAdd) {
		Iterator<String> iterator = toAdd.iterator();
		String value = iterator.next();
		iterator.remove();
		return value;
	}
}
