/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.filoghost.chestcommands.legacy;

import me.filoghost.chestcommands.ChestCommands;
import me.filoghost.chestcommands.config.yaml.PluginConfig;
import me.filoghost.chestcommands.legacy.UpgradesDoneRegistry.UpgradeID;
import me.filoghost.chestcommands.legacy.upgrades.MenuUpgrade;
import me.filoghost.chestcommands.legacy.upgrades.PlaceholdersUpgrade;
import me.filoghost.chestcommands.legacy.upgrades.SettingsUpgrade;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UpgradesExecutor {

	private final ChestCommands plugin;
	private List<Upgrade> failedUpgrades;
	private UpgradesDoneRegistry upgradesDoneRegistry;

	public UpgradesExecutor(ChestCommands plugin) {
		this.plugin = plugin;
	}

	public void run(boolean isFreshInstall) throws UpgradeExecutorException {
		this.failedUpgrades = new ArrayList<>();
		Path upgradesDoneFile = plugin.getDataFolder().toPath().resolve(".upgrades-done");

		try {
			upgradesDoneRegistry = new UpgradesDoneRegistry(upgradesDoneFile);
		} catch (IOException e) {
			// Upgrades can't proceed if metadata file is not read correctly
			throw new UpgradeExecutorException("Couldn't read upgrades metadata file \"" + upgradesDoneFile.getFileName() + "\"", e);
		}

		if (isFreshInstall) {
			// Mark all currently existing upgrades as already done, assuming default configuration files are up to date
			upgradesDoneRegistry.setAllDone();

		} else {
			String legacyCommandSeparator = readLegacyCommandSeparator();

			SettingsUpgrade settingsUpgrade = new SettingsUpgrade(plugin);
			PlaceholdersUpgrade placeholdersUpgrade = new PlaceholdersUpgrade(plugin);
			List<MenuUpgrade> menuUpgrades = getMenuConfigs().stream()
					.map(menuConfig -> new MenuUpgrade(menuConfig, legacyCommandSeparator))
					.collect(Collectors.toList());

			runIfNecessary(UpgradeID.V4_CONFIG, settingsUpgrade);
			runIfNecessary(UpgradeID.V4_PLACEHOLDERS, placeholdersUpgrade);
			runIfNecessary(UpgradeID.V4_MENUS, menuUpgrades);
		}

		try {
			upgradesDoneRegistry.save();
		} catch (IOException e) {
			// Upgrades can't proceed if metadata file is not read correctly
			throw new UpgradeExecutorException("Couldn't save upgrades metadata file \"" + upgradesDoneFile.getFileName() + "\"", e);
		}

		// Success only if no upgrade failed
		if (!failedUpgrades.isEmpty()) {
			String failedConversionFiles = failedUpgrades.stream()
					.map(upgrade -> upgrade.getOriginalFile().getName())
					.collect(Collectors.joining(", "));
			throw new UpgradeExecutorException("Failed to automatically upgrade the following files: " + failedConversionFiles);
		}
	}

	private String readLegacyCommandSeparator() {
		String legacyCommandSeparator;
		PluginConfig settingsConfig = plugin.getSettingsConfig();

		try {
			legacyCommandSeparator = settingsConfig.getString("multiple-commands-separator", ";");
		} catch (Exception e) {
			legacyCommandSeparator = ";";
			plugin.getLogger().log(Level.SEVERE, "Failed to load " + settingsConfig.getFileName()
					+ ", assuming default command separator \"" + legacyCommandSeparator + "\".");
		}

		return legacyCommandSeparator;
	}

	private List<PluginConfig> getMenuConfigs() {
		File menusFolder = plugin.getMenusFolder();
		return plugin.getMenuConfigs(menusFolder);
	}


	private void runIfNecessary(UpgradeID upgradeID, Upgrade upgradeTask) {
		runIfNecessary(upgradeID, Collections.singletonList(upgradeTask));
	}


	private void runIfNecessary(UpgradeID upgradeID, List<? extends Upgrade> upgradeTasks) {
		if (upgradesDoneRegistry.isDone(upgradeID)) {
			return;
		}

		boolean failedAnyUpgrade = false;

		for (Upgrade upgradeTask : upgradeTasks) {
			try {
				boolean modified = upgradeTask.backupAndUpgradeIfNecessary();
				if (modified) {
					plugin.getLogger().info(
							"Automatically upgraded configuration file \""
							+ upgradeTask.getUpgradedFile().getName() + "\" with newer configuration nodes. "
							+ "A backup of the old file has been saved.");
				}
			} catch (UpgradeException e) {
				failedAnyUpgrade = true;
				failedUpgrades.add(upgradeTask);
				logUpgradeException(upgradeTask, e);
			}
		}

		// Upgrade ID is considered complete only if all relative upgrades tasks are successful
		if (!failedAnyUpgrade) {
			upgradesDoneRegistry.setDone(upgradeID);
		}
	}


	private void logUpgradeException(Upgrade upgrade, UpgradeException upgradeException) {
		plugin.getLogger().log(Level.SEVERE,
				"Error while trying to automatically upgrade "	+ upgrade.getOriginalFile() + ": "
				+ upgradeException.getMessage(),
				upgradeException.getCause());
	}

}
