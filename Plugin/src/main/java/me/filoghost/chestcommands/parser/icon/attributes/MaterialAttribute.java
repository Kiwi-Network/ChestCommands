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
package me.filoghost.chestcommands.parser.icon.attributes;

import me.filoghost.chestcommands.menu.icon.AdvancedIcon;
import me.filoghost.chestcommands.parser.ParseException;
import me.filoghost.chestcommands.parser.icon.ApplicableIconAttribute;
import me.filoghost.chestcommands.parser.icon.AttributeErrorCollector;
import me.filoghost.chestcommands.util.MaterialsHelper;
import org.bukkit.Material;

public class MaterialAttribute implements ApplicableIconAttribute {
	
	private final Material material;

	public MaterialAttribute(String serializedMaterial, AttributeErrorCollector attributeErrorCollector) throws ParseException {
		Material material = MaterialsHelper.matchMaterial(serializedMaterial);

		if (material == null || MaterialsHelper.isAir(material)) {
			throw new ParseException("invalid material \"" + serializedMaterial + "\"");
		}

		this.material = material;
	}
	
	@Override
	public void apply(AdvancedIcon icon) {
		icon.setMaterial(material);
	}

}
