package fox.mods.accessdenied.access.criteria;

import fox.mods.accessdenied.access.IAccessCriteria;
import fox.mods.accessdenied.data.GetBounty;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BountyAccess implements IAccessCriteria {
    private Map<ResourceKey<Level>, List<DimensionManager.Bounty>> bountyCriteria;

    private void ensureLoaded() {
        if (bountyCriteria == null) {
            bountyCriteria = loadCriteriaFromJSON();
        }
    }

    private Map<ResourceKey<Level>, List<DimensionManager.Bounty>> loadCriteriaFromJSON() {
        Map<ResourceKey<Level>, List<DimensionManager.Bounty>> bountyCriteria = new HashMap<>();

        for (Map.Entry<ResourceKey<Level>, DimensionManager.DimensionData> entry : DimensionManager.getDimensionData().entrySet()) {
            DimensionManager.DimensionData dimensionData = entry.getValue();
            Optional<DimensionManager.CriteriaData> criteriaData = dimensionData.criteria();
            criteriaData.flatMap(DimensionManager.CriteriaData::bounties).ifPresent(bounties -> {
                bountyCriteria.put(entry.getKey(), bounties);
            });
        }

        return bountyCriteria;
    }

    @Override
    public boolean canAccess(Player player, ResourceKey<Level> dimension) {
        ensureLoaded();
        if (dimension == null) {
            return false;
        }
        List<DimensionManager.Bounty> requiredBounties = bountyCriteria.get(dimension);
        if (requiredBounties == null) {
            return true;
        }

        for (DimensionManager.Bounty bounty : requiredBounties) {
            int playerKillCount = GetBounty.execute(player, bounty.mob());
            if (playerKillCount < bounty.count()) {
                return false;
            }
        }
        return true;
    }

    public Map<ResourceKey<Level>, List<DimensionManager.Bounty>> getBountyCriteria() {
        ensureLoaded();
        return bountyCriteria;
    }
}













