package fox.mods.accessdenied.access.criteria;

import fox.mods.accessdenied.access.IAccessCriteria;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LevelAccess implements IAccessCriteria {
    private Map<ResourceKey<Level>, Integer> levelCriteria;

    private void ensureLoaded() {
        if (levelCriteria == null) {
            levelCriteria = loadCriteriaFromJSON();
        }
    }

    private Map<ResourceKey<Level>, Integer> loadCriteriaFromJSON() {
        Map<ResourceKey<Level>, Integer> levelCriteria = new HashMap<>();

        for (Map.Entry<ResourceKey<Level>, DimensionManager.DimensionData> entry : DimensionManager.getDimensionData().entrySet()) {
            DimensionManager.DimensionData dimensionData = entry.getValue();
            Optional<DimensionManager.CriteriaData> criteriaData = dimensionData.criteria();
            criteriaData.flatMap(DimensionManager.CriteriaData::level).ifPresent(level -> {
                levelCriteria.put(entry.getKey(), level);
            });
        }

        return levelCriteria;
    }

    @Override
    public boolean canAccess(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        if (dimension == null) {
            return false;
        }
        Integer requiredLevel = levelCriteria.get(dimension);
        if (requiredLevel == null) {
            return true;
        }
        int playerLevel = player.experienceLevel;

        return playerLevel >= requiredLevel;
    }

    public Map<ResourceKey<Level>, Integer> getLevelCriteria() {
        ensureLoaded();
        return levelCriteria;
    }
}












