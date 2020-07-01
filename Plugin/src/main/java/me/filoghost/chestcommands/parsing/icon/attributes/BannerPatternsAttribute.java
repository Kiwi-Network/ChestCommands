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
package me.filoghost.chestcommands.parsing.icon.attributes;

import me.filoghost.chestcommands.menu.icon.AdvancedIcon;
import me.filoghost.chestcommands.parsing.ItemMetaParser;
import me.filoghost.chestcommands.parsing.ParseException;
import me.filoghost.chestcommands.parsing.icon.ApplicableIconAttribute;
import me.filoghost.chestcommands.parsing.icon.AttributeErrorCollector;
import org.bukkit.block.banner.Pattern;

import java.util.ArrayList;
import java.util.List;

public class BannerPatternsAttribute implements ApplicableIconAttribute {

	private final List<Pattern> patterns;

	public BannerPatternsAttribute(List<String> serializedPatterns, AttributeErrorCollector attributeErrorCollector) {
		patterns = new ArrayList<>();

		for (String serializedPattern : serializedPatterns) {
			if (serializedPattern == null || serializedPattern.isEmpty()) {
				continue; // Skip
			}

			try {
				Pattern pattern = ItemMetaParser.parseBannerPattern(serializedPattern);
				patterns.add(pattern);
			} catch (ParseException e) {
				attributeErrorCollector.addListElementError(serializedPattern, e);
			}
		}

	}
	
	@Override
	public void apply(AdvancedIcon icon) {
		icon.setBannerPatterns(patterns);
	}

}