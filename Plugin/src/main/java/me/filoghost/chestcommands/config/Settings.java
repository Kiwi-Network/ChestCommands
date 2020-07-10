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
package me.filoghost.chestcommands.config;

import me.filoghost.chestcommands.config.framework.mapped.MappedConfig;
import me.filoghost.chestcommands.config.framework.mapped.modifier.ChatColors;

@ChatColors
public class Settings extends MappedConfig {

	public String default_color__name = "&f";
	public String default_color__lore = "&7";
	public boolean update_notifications = true;
	public int anti_click_spam_delay = 200;

	public Settings() {
		setHeader(
				"ChestCommands main configuration file.",
				"Documentation: https://filoghost.me/docs/chest-commands");
	}

}