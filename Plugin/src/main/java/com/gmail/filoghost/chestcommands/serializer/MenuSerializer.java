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
package com.gmail.filoghost.chestcommands.serializer;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import com.gmail.filoghost.chestcommands.ChestCommands;
import com.gmail.filoghost.chestcommands.api.Icon;
import com.gmail.filoghost.chestcommands.config.ConfigUtil;
import com.gmail.filoghost.chestcommands.config.yaml.PluginConfig;
import com.gmail.filoghost.chestcommands.exception.FormatException;
import com.gmail.filoghost.chestcommands.internal.ExtendedIconMenu;
import com.gmail.filoghost.chestcommands.internal.MenuData;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.serializer.IconSerializer.Coords;
import com.gmail.filoghost.chestcommands.util.ClickType;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;
import com.gmail.filoghost.chestcommands.util.FormatUtils;
import com.gmail.filoghost.chestcommands.util.ItemStackReader;
import com.gmail.filoghost.chestcommands.util.Utils;

public class MenuSerializer {

	private static class Nodes {

		public static final String MENU_NAME = "menu-settings.name";
		public static final String MENU_ROWS = "menu-settings.rows";
		public static final String[] MENU_COMMANDS = {"menu-settings.command", "menu-settings.commands"};

		public static final String[] OPEN_ACTIONS = {"menu-settings.open-actions", "menu-settings.open-action"};

		public static final String[] OPEN_ITEM_MATERIAL = {"menu-settings.open-with-item.id", "menu-settings.open-with-item.material"};
		public static final String OPEN_ITEM_LEFT_CLICK = "menu-settings.open-with-item.left-click";
		public static final String OPEN_ITEM_RIGHT_CLICK = "menu-settings.open-with-item.right-click";

		public static final String AUTO_REFRESH = "menu-settings.auto-refresh";

	}

	public static ExtendedIconMenu loadMenu(PluginConfig config, String title, int rows, ErrorLogger errorLogger) {
		ExtendedIconMenu iconMenu = new ExtendedIconMenu(title, rows, config.getFileName());

		for (String subSectionName : config.getKeys(false)) {
			if (subSectionName.equals("menu-settings")) {
				continue;
			}

			ConfigurationSection iconSection = config.getConfigurationSection(subSectionName);

			Icon icon = IconSerializer.loadIconFromSection(iconSection, subSectionName, config.getFileName(), errorLogger);
			Coords coords = IconSerializer.loadCoordsFromSection(iconSection);

			if (!coords.isSetX() || !coords.isSetY()) {
				errorLogger.addError("The icon \"" + subSectionName + "\" in the menu \"" + config.getFileName() + " is missing POSITION-X and/or POSITION-Y.");
				continue;
			}

			if (iconMenu.getIcon(coords.getX(), coords.getY()) != null) {
				errorLogger.addError("The icon \"" + subSectionName + "\" in the menu \"" + config.getFileName() + " is overriding another icon with the same position.");
			}

			iconMenu.setIcon(coords.getX(), coords.getY(), icon);
		}

		return iconMenu;
	}

	/**
	 * Reads all the settings of a menu. It will never return a null title, even if not set.
	 */
	public static MenuData loadMenuData(PluginConfig config, ErrorLogger errorLogger) {

		String title = FormatUtils.addColors(config.getString(Nodes.MENU_NAME));
		int rows;

		if (title == null) {
			errorLogger.addError("The menu \"" + config.getFileName() + "\" doesn't have a name set.");
			title = ChatColor.DARK_RED + "No title set";
		}

		if (title.length() > 32) {
			title = title.substring(0, 32);
		}

		if (config.isInt(Nodes.MENU_ROWS)) {
			rows = config.getInt(Nodes.MENU_ROWS);

			if (rows <= 0) {
				rows = 1;
			}

		} else {
			rows = 6; // Defaults to 6 rows
			errorLogger.addError("The menu \"" + config.getFileName() + "\" doesn't have a the number of rows set, it will have 6 rows by default.");
		}

		MenuData menuData = new MenuData(title, rows);
		
		List<String> triggeringCommands = ConfigUtil.getStringListOrInlineList(config, ";", Nodes.MENU_COMMANDS);
		if (triggeringCommands != null) {
			menuData.setCommands(triggeringCommands.toArray(new String[0]));
		}

		List<String> serializedOpenCommands = ConfigUtil.getStringListOrInlineList(config, ChestCommands.getSettings().multiple_commands_separator, Nodes.OPEN_ACTIONS);
		
		if (serializedOpenCommands != null && !serializedOpenCommands.isEmpty()) {
			List<IconCommand> openCommands = Utils.newArrayList();
			
			for (String serializedCommand : serializedOpenCommands) {
				if (serializedCommand != null && !serializedCommand.isEmpty()) {
					openCommands.add(CommandSerializer.matchCommand(serializedCommand));
				}
			}

			if (!openCommands.isEmpty()) {
				menuData.setOpenActions(openCommands);
			}
		}

		String openItemMaterial = ConfigUtil.getAnyString(config, Nodes.OPEN_ITEM_MATERIAL);
		if (openItemMaterial != null) {
			try {
				ItemStackReader itemReader = new ItemStackReader(openItemMaterial, false);
				menuData.setBoundMaterial(itemReader.getMaterial());

				if (itemReader.hasExplicitDataValue()) {
					menuData.setBoundDataValue(itemReader.getDataValue());
				}
			} catch (FormatException e) {
				errorLogger.addError("The item \"" + openItemMaterial + "\" used to open the menu \"" + config.getFileName() + "\" is invalid: " + e.getMessage());
			}

			boolean leftClick = config.getBoolean(Nodes.OPEN_ITEM_LEFT_CLICK);
			boolean rightClick = config.getBoolean(Nodes.OPEN_ITEM_RIGHT_CLICK);

			if (leftClick || rightClick) {
				menuData.setClickType(ClickType.fromOptions(leftClick, rightClick));
			}
		}

		if (config.isSet(Nodes.AUTO_REFRESH)) {
			int tenthsToRefresh = (int) (config.getDouble(Nodes.AUTO_REFRESH) * 10.0);
			if (tenthsToRefresh < 1) {
				tenthsToRefresh = 1;
			}
			menuData.setRefreshTenths(tenthsToRefresh);
		}

		return menuData;
	}

}
