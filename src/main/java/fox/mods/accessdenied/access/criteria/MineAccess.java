package fox.mods.accessdenied.access.criteria;

import fox.mods.accessdenied.access.IAccessCriteria;
import fox.mods.accessdenied.data.GetMined;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MineAccess implements IAccessCriteria {
    private Map<ResourceKey<Level>, Integer> mineCriteria;

    private void ensureLoaded() {
        if (mineCriteria == null) {
            mineCriteria = loadCriteriaFromJSON();
        }
    }

    private Map<ResourceKey<Level>, Integer> loadCriteriaFromJSON() {
        Map<ResourceKey<Level>, Integer> mineCriteria = new HashMap<>();

        for (Map.Entry<ResourceKey<Level>, DimensionManager.DimensionData> entry : DimensionManager.getDimensionData().entrySet()) {
            DimensionManager.DimensionData dimensionData = entry.getValue();
            Optional<DimensionManager.CriteriaData> criteriaData = dimensionData.criteria();
            criteriaData.flatMap(DimensionManager.CriteriaData::blocksMined).ifPresent(blocksMined -> {
                mineCriteria.put(entry.getKey(), blocksMined);
            });
        }

        return mineCriteria;
    }

    @Override
    public boolean canAccess(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        if (dimension == null) {
            return false;
        }
        Integer requiredBlocks = mineCriteria.get(dimension);
        if (requiredBlocks == null) {
            return true;
        }
        int blocks = GetMined.execute(player);

        return blocks >= requiredBlocks;
    }

    public Map<ResourceKey<Level>, Integer> getMineCriteria() {
        ensureLoaded();
        return mineCriteria;
    }
}



