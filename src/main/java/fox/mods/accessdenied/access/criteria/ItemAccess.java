package fox.mods.accessdenied.access.criteria;

import fox.mods.accessdenied.access.IAccessCriteria;
import fox.mods.accessdenied.data.ConsumeItem;
import fox.mods.accessdenied.data.GetItem;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemAccess implements IAccessCriteria {
    private Map<ResourceKey<Level>, List<DimensionManager.ItemRequirement>> itemCriteria;

    private void ensureLoaded() {
        if (itemCriteria == null) {
            itemCriteria = loadCriteriaFromJSON();
        }
    }

    private Map<ResourceKey<Level>, List<DimensionManager.ItemRequirement>> loadCriteriaFromJSON() {
        Map<ResourceKey<Level>, List<DimensionManager.ItemRequirement>> itemCriteria = new HashMap<>();

        for (Map.Entry<ResourceKey<Level>, DimensionManager.DimensionData> entry : DimensionManager.getDimensionData().entrySet()) {
            DimensionManager.DimensionData dimensionData = entry.getValue();
            Optional<DimensionManager.CriteriaData> criteriaData = dimensionData.criteria();
            criteriaData.flatMap(DimensionManager.CriteriaData::items).ifPresent(items -> {
                itemCriteria.put(entry.getKey(), items);
            });
        }

        return itemCriteria;
    }

    @Override
    public boolean canAccess(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        if (dimension == null) {
            return false;
        }
        List<DimensionManager.ItemRequirement> requiredItems = itemCriteria.get(dimension);
        if (requiredItems == null) {
            return true;
        }

        for (DimensionManager.ItemRequirement items : requiredItems) {
            int itemCount = GetItem.execute(player, items.item() != null ? items.item().location() : null, items.tag());
            if (itemCount < items.count()) {
                return false;
            }
        }
        return true;
    }

    public void consumeItems(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        List<DimensionManager.ItemRequirement> requiredItems = itemCriteria.get(dimension);
        if (requiredItems == null) {
            return;
        }

        // Consume the items
        for (DimensionManager.ItemRequirement items : requiredItems) {
            ConsumeItem.execute(player, items.item() != null ? items.item().location() : null, items.tag(), items.consume());
        }
    }

    public Map<ResourceKey<Level>, List<DimensionManager.ItemRequirement>> getItemCriteria() {
        ensureLoaded();
        return itemCriteria;
    }
}




