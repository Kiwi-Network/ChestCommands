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
package me.filoghost.chestcommands.config.files;

import me.filoghost.chestcommands.config.SpecialConfig;

public class Lang extends SpecialConfig {

	public final String no_open_permission = "&cYou don't have permission &e{permission} &cto use this menu.";
	public final String default_no_icon_permission = "&cYou don't have permission for this icon.";
	public final String no_required_item = "&cYou must have &e{amount}x {material} &c(durability: {durability}) for this.";
	public final String no_money = "&cYou need {money}$ for this.";
	public final String no_exp = "&cYou need {levels} XP levels for this.";
	public final String menu_not_found = "&cMenu not found! Please inform the staff.";
	public final String open_menu = "&aOpening the menu \"{menu}\".";
	public final String open_menu_others = "&aOpening the menu \"{menu}\" to {player}.";
	public final String any = "any"; // Used in no_required_item when durability is not restrictive

}
