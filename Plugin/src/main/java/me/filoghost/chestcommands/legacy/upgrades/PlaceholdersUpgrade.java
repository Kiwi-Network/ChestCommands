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

import me.filoghost.chestcommands.ChestCommands;
import me.filoghost.chestcommands.config.yaml.PluginConfig;
import me.filoghost.chestcommands.legacy.Upgrade;
import me.filoghost.chestcommands.legacy.UpgradeException;
import me.filoghost.chestcommands.util.Strings;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class PlaceholdersUpgrade extends Upgrade {

	private final PluginConfig newPlaceholdersConfig;
	private final File oldPlaceholdersFile;

	private List<String> lines;

	public PlaceholdersUpgrade(ChestCommands plugin) {
		this.newPlaceholdersConfig = plugin.getPlaceholdersConfig();
		this.oldPlaceholdersFile = new File(plugin.getDataFolder(), "placeholders.yml");
	}

	@Override
	public File getOriginalFile() {
		return oldPlaceholdersFile;
	}

	@Override
	public File getUpgradedFile() {
		return newPlaceholdersConfig.getFile();
	}

	@Override
	protected void computeChanges() throws UpgradeException {
		if (!oldPlaceholdersFile.isFile()) {
			return;
		}

		// Do NOT load the new placeholder configuration from disk, as it should only contain placeholders imported from the old file

		try {
			lines = Files.readAllLines(oldPlaceholdersFile.toPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UpgradeException("couldn't read file \"" + oldPlaceholdersFile.getName() + "\"", e);
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

			newPlaceholdersConfig.set(placeholder, replacement);
			setModified();
		}
	}

	@Override
	protected void saveChanges() throws IOException {
		oldPlaceholdersFile.delete();
		newPlaceholdersConfig.save();
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