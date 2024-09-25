package fox.mods.accessdenied.access;

import fox.mods.accessdenied.access.criteria.ItemAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class AccessManager {
    private final List<IAccessCriteria> criteriaList = new ArrayList<>();

    public void addCriteria(IAccessCriteria criteria) {
        criteriaList.add(criteria);
    }

    public boolean canEnterDimension(Player player, ResourceKey<Level> dimension) {
        for (IAccessCriteria criteria : criteriaList) {
            if (!criteria.canAccess(player, dimension)) {
                return false;
            }
        }
        return true;
    }

    public void handleItemConsumption(Player player, ResourceKey<Level> dimension) {
        for (IAccessCriteria criteria : criteriaList) {
            if (criteria instanceof ItemAccess) {
                ((ItemAccess) criteria).consumeItems(player, dimension);
            }
        }
    }
}
