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
package me.filoghost.chestcommands.action;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.filoghost.chestcommands.parser.FormatException;
import me.filoghost.chestcommands.parser.ItemStackParser;

public class GiveItemAction extends Action {

	private ItemStack itemToGive;

	public GiveItemAction(String serializedAction) {
		try {
			ItemStackParser reader = new ItemStackParser(serializedAction, true);
			itemToGive = reader.createStack();
		} catch (FormatException e) {
			disable(ChatColor.RED + "Invalid item to give: " + e.getMessage());
		}
	}

	@Override
	protected void executeInner(Player player) {
		player.getInventory().addItem(itemToGive.clone());
	}

}
