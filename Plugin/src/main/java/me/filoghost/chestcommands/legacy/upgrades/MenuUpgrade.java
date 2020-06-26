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
package me.filoghost.chestcommands.legacy.upgrades;

import me.filoghost.chestcommands.config.yaml.PluginConfig;
import me.filoghost.chestcommands.legacy.Upgrade;
import me.filoghost.chestcommands.legacy.UpgradeException;
import me.filoghost.chestcommands.util.Strings;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class MenuUpgrade extends Upgrade {

	private final PluginConfig menuConfig;
	private final String legacyCommandSeparator;

	public MenuUpgrade(PluginConfig menuConfig, String legacyCommandSeparator) {
		this.menuConfig = menuConfig;
		this.legacyCommandSeparator = legacyCommandSeparator;
	}

	@Override
	public File getOriginalFile() {
		return menuConfig.getFile();
	}

	@Override
	public File getUpgradedFile() {
		return menuConfig.getFile();
	}

	@Override
	protected void computeChanges() throws UpgradeException {
		loadConfig(menuConfig);

		for (String key : menuConfig.getKeys(true)) {
			if (!menuConfig.isConfigurationSection(key)) {
				continue;
			}

			ConfigurationSection section = menuConfig.getConfigurationSection(key);

			if (key.equals("menu-settings")) {
				upgradeMenuSettings(section);
			} else {
				upgradeIcon(section);
			}
		}
	}

	@Override
	protected void saveChanges() throws IOException {
		menuConfig.save();
	}


	private void upgradeMenuSettings(ConfigurationSection section) {
		renameNode(section, "command", "commands");
		renameNode(section, "open-action", "open-actions");
		renameNode(section, "open-with-item.id", "open-with-item.material");

		expandInlineList(section, "commands", ";");
		expandInlineList(section, "open-actions", legacyCommandSeparator);
	}

	private void upgradeIcon(ConfigurationSection section) {
		renameNode(section, "ID", "MATERIAL");
		renameNode(section, "DATA-VALUE", "DURABILITY");
		renameNode(section, "NBT", "NBT-DATA");
		renameNode(section, "ENCHANTMENT", "ENCHANTMENTS");
		renameNode(section, "COMMAND", "ACTIONS");
		renameNode(section, "COMMANDS", "ACTIONS");
		renameNode(section, "REQUIRED-ITEM", "REQUIRED-ITEMS");

		expandInlineList(section, "ACTIONS", legacyCommandSeparator);
		expandInlineList(section, "ENCHANTMENTS", ";");

		expandSingletonList(section, "REQUIRED-ITEMS");

		expandInlineItemstack(section);
	}

	private void expandInlineItemstack(ConfigurationSection section) {
		String material = section.getString("MATERIAL");
		if (material == null) {
			return;
		}

		if (material.contains(",")) {
			String[] parts = Strings.trimmedSplit(material, ",", 2);
			if (!section.isSet("AMOUNT")) {
				try {
					section.set("AMOUNT", Integer.parseInt(parts[1]));
				} catch (NumberFormatException e) {
					section.set("AMOUNT", parts[1]);
				}
			}
			material = parts[0];
			section.set("MATERIAL", material);
			setModified();
		}

		if (material.contains(":")) {
			String[] parts = Strings.trimmedSplit(material, ":", 2);
			if (!section.isSet("DURABILITY")) {
				try {
					section.set("DURABILITY", Integer.parseInt(parts[1]));
				} catch (NumberFormatException e) {
					section.set("DURABILITY", parts[1]);
				}
			}
			material = parts[0];
			section.set("MATERIAL", material);
			setModified();
		}
	}

	private void renameNode(ConfigurationSection config, String oldNode, String newNode) {
		if (config.isSet(oldNode) && !config.isSet(newNode)) {
			config.set(newNode, config.get(oldNode));
			config.set(oldNode, null);
			setModified();
		}
	}

	private void expandInlineList(ConfigurationSection config, String node, String separator) {
		if (config.isSet(node)) {
			if (config.isString(node)) {
				config.set(node, getSeparatedValues(config.getString(node), separator));
				setModified();
			}
		}
	}

	private void expandSingletonList(ConfigurationSection config, String node) {
		if (config.isSet(node)) {
			config.set(node, Collections.singletonList(config.get(node)));
			setModified();
		}
	}

	private List<String> getSeparatedValues(String input, String separator) {
		if (separator == null || separator.length() == 0) {
			separator = ";";
		}

		String[] splitValues = Strings.trimmedSplit(input, Pattern.quote(separator));
		List<String> values = new ArrayList<>();

		for (String value : splitValues) {
			if (!value.isEmpty()) {
				values.add(value);
			}
		}

		// Return a list with an empty value to avoid displaying the empty list value "[]" in the YML file
		if (values.isEmpty()) {
			values.add("");
		}

		return values;
	}

}