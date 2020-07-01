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
package me.filoghost.chestcommands.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.filoghost.chestcommands.ChestCommands;
import me.filoghost.chestcommands.action.*;

public class ActionParser {

	private static final Map<Pattern, IconCommandFactory> actionsByPrefix = new HashMap<>();

	static {
		actionsByPrefix.put(actionPattern("console:"), ConsoleCommandAction::new);
		actionsByPrefix.put(actionPattern("op:"), OpCommandAction::new);
		actionsByPrefix.put(actionPattern("(open|menu):"), OpenMenuAction::new);
		actionsByPrefix.put(actionPattern("server:?"), ChangeServerAction::new); // The colon is optional
		actionsByPrefix.put(actionPattern("tell:"), SendMessageAction::new);
		actionsByPrefix.put(actionPattern("broadcast:"), BroadcastAction::new);
		actionsByPrefix.put(actionPattern("give:"), GiveItemAction::new);
		actionsByPrefix.put(actionPattern("give-?money:"), GiveMoneyAction::new);
		actionsByPrefix.put(actionPattern("sound:"), PlaySoundAction::new);
		actionsByPrefix.put(actionPattern("dragon-?bar:"), DragonBarAction::new);
	}

	private static Pattern actionPattern(String regex) {
		return Pattern.compile("^" + regex, Pattern.CASE_INSENSITIVE); // Case insensitive and only at the beginning
	}

	public static Action parseAction(String input) {
		for (Entry<Pattern, IconCommandFactory> entry : actionsByPrefix.entrySet()) {
			Matcher matcher = entry.getKey().matcher(input);
			if (matcher.find()) {
				// Remove the action prefix and trim the spaces
				String serializedAction = matcher.replaceFirst("").trim();
				return entry.getValue().create(ChestCommands.getCustomPlaceholders().replaceAll(serializedAction));
			}
		}

		return new PlayerCommandAction(ChestCommands.getCustomPlaceholders().replaceAll(input)); // Default action, no match found
	}
	
	
	private interface IconCommandFactory {
		
		Action create(String actionString);
		
	}

}