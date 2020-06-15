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
package me.filoghost.chestcommands.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.filoghost.chestcommands.menu.AdvancedIconMenu;
import me.filoghost.chestcommands.menu.MenuManager;
import me.filoghost.chestcommands.menu.MenuView;

public class RefreshMenusTask implements Runnable {

	private long elapsedTenths;

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			MenuView openMenuView = MenuManager.getOpenMenuView(player);
			if (openMenuView == null) {
				return;
			}
			
			if (!(openMenuView.getMenu() instanceof AdvancedIconMenu)) {
				return;
			}
			
			AdvancedIconMenu iconMenu = (AdvancedIconMenu) openMenuView.getMenu();
			
			if (elapsedTenths % iconMenu.getRefreshTicks() == 0) {
				iconMenu.refresh(player, openMenuView.getInventory());
			}
		}

		elapsedTenths++;
	}

}
