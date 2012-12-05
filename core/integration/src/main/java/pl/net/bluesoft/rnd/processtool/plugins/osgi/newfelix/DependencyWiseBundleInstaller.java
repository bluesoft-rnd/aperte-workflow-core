package pl.net.bluesoft.rnd.processtool.plugins.osgi.newfelix;

import org.osgi.framework.Bundle;

import java.util.*;
import java.util.logging.Logger;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-11-27
 * Time: 20:33
 *
 * This implementation is intended to replace original one. Needs extensive testing.
 */
public class DependencyWiseBundleInstaller extends BundleInstaller {
	private Set<String> bundlePathsToRemove;
	private Set<String> bundlePathsToInstall;

	public DependencyWiseBundleInstaller(NewFelixBundleService felixService, BundleInfo bundleInfo, Logger logger) {
		super(felixService, bundleInfo, logger);
	}

	public void syncBundlesWithRepository() {
		getBundleInfo().installationStarted();
		getBundleInfo().updateBundleRepositoryStatus();

		determineAffectedBundles();

		removeBundles();

		if (!bundlePathsToInstall.isEmpty()) {
			installBundles();
		}

		getBundleInfo().installationFinished();
	}

	private void determineAffectedBundles() {
		List<String> installedBundlePaths = getFelixService().getInstalledBundlePaths();
		Collection<String> uninstalled = from(installedBundlePaths).except(getBundleInfo().getExistingBundlePaths());
		Collection<String> toReinstall = from(installedBundlePaths).intersect(getBundleInfo().getInstallableBundlePaths());

		bundlePathsToRemove = from(uninstalled).union(toReinstall).toSet();
		bundlePathsToRemove = getBundleInfo().getInverseDependencyClosure(bundlePathsToRemove);

		bundlePathsToInstall = from(getBundleInfo().getInstallableBundlePaths()).toSet();
		bundlePathsToInstall = from(bundlePathsToInstall).union(getBundleInfo().getInverseDependencyClosure(toReinstall)).toSet();
	}

	private void installBundles() {
		getBundleInfo().updateDependencyInfo();

		List<BundleCommand> commands = new InstallBundleCommandFactory().createCommands();

		executeCommands(commands);
	}

	private void removeBundles() {
		List<BundleCommand> commands = new UninstallBundleCommandFactory().createCommands();

		executeCommands(commands);
	}

	private abstract class BundleCommand {
		protected final List<String> bundlesToBeInstalled = new ArrayList<String>();
		protected final List<String> bundlesToBeUninstalled = new ArrayList<String>();
		protected final Set<String> executedWith = new HashSet<String>();

		protected final String bundlePath;

		protected BundleCommand(String bundlePath) {
			this.bundlePath = bundlePath;
		}

		public boolean invoke() {
			return meetsConditions() && doInvoke();
		}

		protected abstract boolean doInvoke();

		public void addBundlesToBeInstalled(Collection<String> bundlePaths) {
			bundlesToBeInstalled.addAll(bundlePaths);
		}

		public void addBundlesToBeUninstalled(Collection<String> bundlePaths) {
			bundlesToBeUninstalled.addAll(bundlePaths);
		}

		public void addExecutedWith(Collection<String> bundlePaths) {
			executedWith.addAll(bundlePaths);
		}

		protected boolean meetsConditions() {
			for (String bundlePath : bundlesToBeInstalled) {
				if (getBundleInfo().getBundleStatus(bundlePath) != BundleInfo.Status.INSTALLATION_SUCCEEDED) {
					return false;
				}
			}
			for (String bundlePath : bundlesToBeUninstalled) {
				if (getBundleInfo().getBundleStatus(bundlePath) != BundleInfo.Status.UNINSTALLATION_SUCCEEDED) {
					return false;
				}
			}
			return true;
		}

		public boolean retryLater() {
			return false;
		}

		public abstract Collection<String> getDependenciesToBeReached();

		public String getBundlePath() {
			return bundlePath;
		}

		@Override
		public String toString() {
			return getBundlePath();
		}
	}

	private class InstallBundleCommand extends BundleCommand {
		public InstallBundleCommand(String bundlePath) {
			super(bundlePath);
		}

		@Override
		protected boolean doInvoke() {
			return getFelixService().doInstallBundle(bundlePath);
		}

		@Override
		public Collection<String> getDependenciesToBeReached() {
			return from(bundlesToBeInstalled).intersect(executedWith);
		}
	}

	private class UninstallBundleCommand extends BundleCommand {
		public UninstallBundleCommand(String bundlePath) {
			super(bundlePath);
		}

		@Override
		protected boolean doInvoke() {
			Bundle bundle = getFelixService().getBundle(bundlePath);
			return bundle == null || getFelixService().doUninstallBundle(bundle);
		}

		@Override
		public Collection<String> getDependenciesToBeReached() {
			return from(bundlesToBeUninstalled).intersect(executedWith);
		}
	}

	private <T extends BundleCommand> void executeCommands(List<T> commands) {
		while (!commands.isEmpty()) {
			T command = commands.remove(0);

			if (!command.invoke()) {
				if (command.retryLater() && !commands.isEmpty()) {
					commands.add(1, command);
				}
			}
		}
	}

	private abstract class BundleCommandFactory {
		public List<BundleCommand> createCommands() {
			Collection<String> bundlePaths = getAffectedBundlePaths();
			List<BundleCommand> commands = new ArrayList<BundleCommand>();

			for (String bundlePath : bundlePaths) {
				commands.add(createCommand(bundlePath));
			}
			return determineExecutionOrder(commands);
		}

		protected abstract BundleCommand createCommand(String bundlePath);
		protected abstract Collection<String> getAffectedBundlePaths();

		private List<BundleCommand> determineExecutionOrder(List<BundleCommand> commands) {
			List<BundleCommand> result = new ArrayList<BundleCommand>();

			if (commands.isEmpty()) {
				return result;
			}

			Set<String> reachedDependencies = new HashSet<String>();

			// plugins that have no dependencies to be installed

			for (BundleCommand command : commands) {
				if (command.getDependenciesToBeReached().isEmpty()) {
					result.add(command);
					reachedDependencies.add(command.getBundlePath());
				}
			}

			Set<BundleCommand> toSort = new HashSet<BundleCommand>(commands);
			toSort.removeAll(result);

			while (!toSort.isEmpty()) {
				boolean loaded = false;
				for (BundleCommand dep : toSort) {
					if (reachedDependencies.containsAll(dep.getDependenciesToBeReached())) {
						result.add(dep);
						reachedDependencies.add(dep.getBundlePath());
						toSort.remove(dep);
						loaded = true;
						break;
					}
				}
				if (!loaded) {
					break;
				}
			}

			// remaining plugins to be installed in the end

			for (BundleCommand dep : toSort) {
				getLogger().severe(dep.getBundlePath() + " may not be installed because of failed dependency resolution");
			}
			result.addAll(toSort);

			return result;
		}
	}

	private class InstallBundleCommandFactory extends BundleCommandFactory {
		@Override
		protected BundleCommand createCommand(String bundlePath) {
			InstallBundleCommand command = new InstallBundleCommand(bundlePath);
			command.addBundlesToBeInstalled(getBundleInfo().getBundleDependencies(bundlePath));
			command.addExecutedWith(getAffectedBundlePaths());
			return command;
		}

		@Override
		protected Collection<String> getAffectedBundlePaths() {
			return bundlePathsToInstall;
		}
	}

	private class UninstallBundleCommandFactory extends BundleCommandFactory {
		@Override
		protected BundleCommand createCommand(String bundlePath) {
			UninstallBundleCommand command = new UninstallBundleCommand(bundlePath);
			command.addBundlesToBeUninstalled(getBundleInfo().getInverseBundleDependencies(bundlePath));
			command.addExecutedWith(getAffectedBundlePaths());
			return command;
		}

		@Override
		protected Collection<String> getAffectedBundlePaths() {
			return bundlePathsToRemove;
		}
	}

}
