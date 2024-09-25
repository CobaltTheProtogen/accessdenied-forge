package fox.mods.accessdenied.data;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GetItem {
	public static int execute(Player player, ResourceLocation item, TagKey<Item> tag) {
		if (player == null) {
			return 0;
		}
		int count = 0;

		if (item != null) {
			// Check inventory
			for (ItemStack itemStack : player.getInventory().items) {
				ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
				if (resourceLocation.equals(item)) {
					count += itemStack.getCount();
				}
			}
			// Check equipment slots
			for (ItemStack itemStack : player.getInventory().armor) {
				ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
				if (resourceLocation.equals(item)) {
					count += itemStack.getCount();
				}
			}
			// Check offhand
			for (ItemStack itemStack : player.getInventory().offhand) {
				ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
				if (resourceLocation.equals(item)) {
					count += itemStack.getCount();
				}
			}
		}

		if (tag != null) {
			// Check inventory
			for (ItemStack itemStack : player.getInventory().items) {
				if (itemStack.getTags().anyMatch(tag::equals)) {
					count += itemStack.getCount();
				}
			}
			// Check equipment slots
			for (ItemStack itemStack : player.getInventory().armor) {
				if (itemStack.getTags().anyMatch(tag::equals)) {
					count += itemStack.getCount();
				}
			}
			// Check offhand
			for (ItemStack itemStack : player.getInventory().offhand) {
				if (itemStack.getTags().anyMatch(tag::equals)) {
					count += itemStack.getCount();
				}
			}
		}
		return count;
	}
}




