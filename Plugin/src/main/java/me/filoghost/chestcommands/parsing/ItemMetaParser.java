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

import me.filoghost.chestcommands.util.Registry;
import me.filoghost.chestcommands.util.Strings;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;

public final class ItemMetaParser {
	
	private static final Registry<DyeColor> DYE_COLORS_REGISTRY = Registry.fromEnumValues(DyeColor.class);
	private static final Registry<PatternType> PATTERN_TYPES_REGISTRY = Registry.fromEnumValues(PatternType.class);

	private ItemMetaParser() {}

	
	public static Color parseRGBColor(String input) throws ParseException {
		String[] split = Strings.trimmedSplit(input, ",");

		if (split.length != 3) {
			throw new ParseException("it must be in the format \"red, green, blue\"");
		}

		int red = NumberParser.getInteger(split[0], "red is not a number");
		int green = NumberParser.getInteger(split[1], "green is not a number");
		int blue = NumberParser.getInteger(split[2], "blue is not a number");

		if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
			throw new ParseException("it should only contain numbers between 0 and 255");
		}

		return Color.fromRGB(red, green, blue);
	}

	public static DyeColor parseDyeColor(String input) throws ParseException {
		return DYE_COLORS_REGISTRY.find(input)
				.orElseThrow(() -> new ParseException("it must be a valid color"));
	}

	public static Pattern parseBannerPattern(String input) throws ParseException {
		String[] split = Strings.trimmedSplit(input, ":");
		if (split.length != 2) {
			throw new ParseException("it must be in the format \"pattern:color\"");
		}

		PatternType patternType = PATTERN_TYPES_REGISTRY.find(split[0])
				.orElseThrow(() -> new ParseException("it must be a valid pattern type"));
		DyeColor patternColor = parseDyeColor(split[1]);

		return new Pattern(patternColor, patternType);
	}
}
