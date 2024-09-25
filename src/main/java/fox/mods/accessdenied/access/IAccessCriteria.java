package fox.mods.accessdenied.access;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IAccessCriteria {
    boolean canAccess(Player player, ResourceKey<Level> dimension);
}
