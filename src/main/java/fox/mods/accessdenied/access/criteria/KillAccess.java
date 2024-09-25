package fox.mods.accessdenied.access.criteria;

import fox.mods.accessdenied.access.IAccessCriteria;
import fox.mods.accessdenied.data.GetKills;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KillAccess implements IAccessCriteria {
    private Map<ResourceKey<Level>, Integer> killCriteria;

    private void ensureLoaded() {
        if (killCriteria == null) {
            killCriteria = loadCriteriaFromJSON();
        }
    }

    private Map<ResourceKey<Level>, Integer> loadCriteriaFromJSON() {
        Map<ResourceKey<Level>, Integer> killCriteria = new HashMap<>();

        for (Map.Entry<ResourceKey<Level>, DimensionManager.DimensionData> entry : DimensionManager.getDimensionData().entrySet()) {
            DimensionManager.DimensionData dimensionData = entry.getValue();
            Optional<DimensionManager.CriteriaData> criteriaData = dimensionData.criteria();
            criteriaData.flatMap(DimensionManager.CriteriaData::mobs).ifPresent(mobs -> {
                killCriteria.put(entry.getKey(), mobs);
            });
        }

        return killCriteria;
    }

    @Override
    public boolean canAccess(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        if (dimension == null) {
            return false;
        }
        Integer requiredKills = killCriteria.get(dimension);
        if (requiredKills == null) {
            return true;
        }
        int kills = GetKills.execute(player);

        return kills >= requiredKills;
    }

    public Map<ResourceKey<Level>, Integer> getKillCriteria() {
        ensureLoaded();
        return killCriteria;
    }
}











