package fox.mods.accessdenied.access.criteria;

import fox.mods.accessdenied.access.IAccessCriteria;
import fox.mods.accessdenied.data.GetPlaced;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlaceAccess implements IAccessCriteria {
    private Map<ResourceKey<Level>, Integer> placeCriteria;

    private void ensureLoaded() {
        if (placeCriteria == null) {
            placeCriteria = loadCriteriaFromJSON();
        }
    }

    private Map<ResourceKey<Level>, Integer> loadCriteriaFromJSON() {
        Map<ResourceKey<Level>, Integer> placeCriteria = new HashMap<>();

        for (Map.Entry<ResourceKey<Level>, DimensionManager.DimensionData> entry : DimensionManager.getDimensionData().entrySet()) {
            DimensionManager.DimensionData dimensionData = entry.getValue();
            Optional<DimensionManager.CriteriaData> criteriaData = dimensionData.criteria();
            criteriaData.flatMap(DimensionManager.CriteriaData::blocksPlaced).ifPresent(blocksPlaced -> {
                placeCriteria.put(entry.getKey(), blocksPlaced);
            });
        }

        return placeCriteria;
    }

    @Override
    public boolean canAccess(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        if (dimension == null) {
            return false;
        }
        Integer requiredBlocks = placeCriteria.get(dimension);
        if (requiredBlocks == null) {
            return true;
        }
        int blocks = GetPlaced.execute(player);

        return blocks >= requiredBlocks;
    }

    public Map<ResourceKey<Level>, Integer> getPlacementCriteria() {
        ensureLoaded();
        return placeCriteria;
    }
}



