/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package me.filoghost.chestcommands.legacy.v4_0;

import me.filoghost.chestcommands.config.ConfigManager;
import me.filoghost.chestcommands.config.framework.Config;
import me.filoghost.chestcommands.config.framework.ConfigLoader;
import me.filoghost.chestcommands.config.framework.exception.ConfigLoadException;
import me.filoghost.chestcommands.config.framework.exception.ConfigSaveException;
import me.filoghost.chestcommands.legacy.upgrade.UpgradeTask;
import me.filoghost.chestcommands.logging.ErrorMessages;
import me.filoghost.chestcommands.util.Strings;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class v4_0_PlaceholdersFileUpgradeTask extends UpgradeTask {

	private final Path oldPlaceholdersFile;
	private final ConfigLoader newPlaceholdersConfigLoader;
	private Config updatedConfig;

	public v4_0_PlaceholdersFileUpgradeTask(ConfigManager configManager) {
		this.oldPlaceholdersFile = configManager.getRootDataFolder().resolve("placeholders.yml");
		this.newPlaceholdersConfigLoader = configManager.getConfigLoader("custom-placeholders.yml");
	}

	@Override
	public Path getOriginalFile() {
		return oldPlaceholdersFile;
	}

	@Override
	public Path getUpgradedFile() {
		return newPlaceholdersConfigLoader.getFile();
	}

	@Override
	public void computeChanges() throws ConfigLoadException {
		if (!Files.isRegularFile(oldPlaceholdersFile)) {
			return;
		}

		// Do NOT load the new placeholder configuration from disk, as it should only contain placeholders imported from the old file
		Config newPlaceholdersConfig = new Config(newPlaceholdersConfigLoader.getFile());
		List<String> lines;
		try {
			lines = Files.readAllLines(oldPlaceholdersFile);
		} catch (IOException e) {
			throw new ConfigLoadException(ErrorMessages.Config.readIOException, e);
		}

		for (String line : lines) {
			// Comment or empty line
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			// Ignore bad line
			if (!line.contains(":")) {
				continue;
			}

			String[] parts = Strings.trimmedSplit(line, ":", 2);
			String placeholder = unquote(parts[0]);
			String replacement = StringEscapeUtils.unescapeJava(unquote(parts[1]));

			newPlaceholdersConfig.setString("placeholders." + placeholder, replacement);
			setSaveRequired();
		}

		this.updatedConfig = newPlaceholdersConfig;
	}

	@Override
	public void saveChanges() throws ConfigSaveException {
		try {
			Files.deleteIfExists(oldPlaceholdersFile);
		} catch (IOException ignored) {}
		newPlaceholdersConfigLoader.save(updatedConfig);
	}

	private static String unquote(String input) {
		if (input.length() < 2) {
			// Too short, cannot be a quoted string
			return input;
		}
		if (input.startsWith("'") && input.endsWith("'")) {
			return input.substring(1, input.length() - 1);
		} else if (input.startsWith("\"") && input.endsWith("\"")) {
			return input.substring(1, input.length() - 1);
		}

		return input;
	}

}