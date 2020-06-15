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
package me.filoghost.chestcommands.menu;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import me.filoghost.chestcommands.menu.inventory.MenuInventoryHolder;
import me.filoghost.chestcommands.menu.settings.OpenTrigger;
import me.filoghost.chestcommands.util.CaseInsensitiveMap;
import me.filoghost.chestcommands.util.ErrorCollector;

public class MenuManager {
	
	private static Map<String, AdvancedIconMenu> menusByFile;
	private static Map<String, AdvancedIconMenu> menusByCommand;
	private static Map<OpenTrigger, AdvancedIconMenu> menusByOpenTrigger;
	
	public MenuManager() {
		menusByFile = CaseInsensitiveMap.create();
		menusByCommand = CaseInsensitiveMap.create();
		menusByOpenTrigger = new HashMap<>();
	}
	
	public void clear() {
		menusByFile.clear();
		menusByCommand.clear();
		menusByOpenTrigger.clear();
	}

	public AdvancedIconMenu getMenuByFileName(String fileName) {
		return menusByFile.get(fileName);
	}

	public void registerMenu(String fileName, Collection<String> triggerCommands, AdvancedIconMenu menu, ErrorCollector errorCollector) {
		if (menusByFile.containsKey(fileName)) {
			errorCollector.addError("Two menus have the same file name \"" + fileName + "\" with different cases. There will be problems opening one of these two menus.");
		}
		
		menusByFile.put(fileName, menu);

		for (String triggerCommand : triggerCommands) {
			if (!triggerCommand.isEmpty()) {
				if (menusByCommand.containsKey(triggerCommand)) {
					errorCollector.addError("The menus \"" + menusByCommand.get(triggerCommand).getFileName() + "\" and \"" + fileName + "\" have the same command \"" + triggerCommand + "\". Only one will be opened.");
				}
				menusByCommand.put(triggerCommand, menu);
			}
		}		
	}

	public void registerTriggerItem(OpenTrigger openTrigger, AdvancedIconMenu menu) {
		menusByOpenTrigger.put(openTrigger, menu);
	}

	public void openMenuByItem(Player player, ItemStack itemInHand, Action clickAction) {
		menusByOpenTrigger.forEach((openTrigger, menu) -> {
			if (openTrigger.matches(itemInHand, clickAction)) {
				menu.openCheckingPermission(player);
			}
		});
	}

	public AdvancedIconMenu getMenuByCommand(String command) {
		return menusByCommand.get(command);
	}

	public Collection<String> getMenuFileNames() {
		return Collections.unmodifiableCollection(menusByFile.keySet());
	}
	
	
	public static BaseIconMenu<?> getOpenMenu(Player player) {
		InventoryView view = player.getOpenInventory();
		if (view == null) {
			return null;
		}
		
		BaseIconMenu<?> openMenu = getOpenMenu(view.getTopInventory());
		if (openMenu == null) {
			openMenu = getOpenMenu(view.getBottomInventory());
		}
		
		return openMenu;
	}
	
	
	public static BaseIconMenu<?> getOpenMenu(Inventory inventory) {
		if (!(inventory.getHolder() instanceof MenuInventoryHolder)) {
			return null;
		}
		
		return ((MenuInventoryHolder) inventory.getHolder()).getIconMenu();
	}
	
	
	public static MenuView getOpenMenuView(Player player) {
		InventoryView view = player.getOpenInventory();
		if (view == null) {
			return null;
		}
		
		MenuView openMenuView = getOpenMenuView(view.getTopInventory());
		if (openMenuView == null) {
			openMenuView = getOpenMenuView(view.getBottomInventory());
		}
		
		return openMenuView;
	}
	
	
	private static MenuView getOpenMenuView(Inventory inventory) {
		if (!(inventory.getHolder() instanceof MenuInventoryHolder)) {
			return null;
		}
		
		MenuInventoryHolder menuInventoryHolder = (MenuInventoryHolder) inventory.getHolder();
		if (!(menuInventoryHolder.getIconMenu() instanceof AdvancedIconMenu)) {
			return null;
		}
			
		return new MenuView((AdvancedIconMenu) menuInventoryHolder.getIconMenu(), inventory);		
	}

}
