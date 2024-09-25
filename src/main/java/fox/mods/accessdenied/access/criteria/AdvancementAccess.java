package fox.mods.accessdenied.access.criteria;

import fox.mods.accessdenied.access.IAccessCriteria;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;

public class AdvancementAccess implements IAccessCriteria {
    private Map<ResourceKey<Level>, List<ResourceKey<Advancement>>> advancementCriteria;

    private void ensureLoaded() {
        if (advancementCriteria == null) {
            advancementCriteria = loadCriteriaFromJSON();
        }
    }

    private Map<ResourceKey<Level>, List<ResourceKey<Advancement>>> loadCriteriaFromJSON() {
        Map<ResourceKey<Level>, List<ResourceKey<Advancement>>> advancementCriteria = new HashMap<>();

        for (Map.Entry<ResourceKey<Level>, DimensionManager.DimensionData> entry : DimensionManager.getDimensionData().entrySet()) {
            DimensionManager.DimensionData dimensionData = entry.getValue();
            Optional<DimensionManager.CriteriaData> criteriaData = dimensionData.criteria();
            criteriaData.flatMap(DimensionManager.CriteriaData::advancements).ifPresent(advancements -> {
                advancementCriteria.put(entry.getKey(), advancements);
            });
        }

        return advancementCriteria;
    }

    @Override
    public boolean canAccess(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        if (dimension == null) {
            return false;
        }
        List<ResourceKey<Advancement>> requiredAdvancements = advancementCriteria.get(dimension);
        if (requiredAdvancements == null) {
            return true;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            for (ResourceKey<Advancement> advancementKey : requiredAdvancements) {
                Advancement advancement = Objects.requireNonNull(serverPlayer.server.getAdvancements().get(advancementKey.location())).value();
                if (advancement == null) {
                    return false;
                }
                AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(new AdvancementHolder(advancementKey.location(), advancement));
                if (!progress.isDone()) {
                    return false;
                }
            }
        }

        return true;
    }

    public Map<ResourceKey<Level>, List<ResourceKey<Advancement>>> getAdvancementCriteria() {
        ensureLoaded();
        return advancementCriteria;
    }
}

