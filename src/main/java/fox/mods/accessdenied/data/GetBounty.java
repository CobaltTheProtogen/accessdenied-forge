package fox.mods.accessdenied.data;


import fox.mods.accessdenied.network.AccessDeniedModVariables;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class GetBounty {
    public static int execute(Player player, String mob) {
        if (player == null || mob == null || mob.isEmpty()) {
            return 0;
        }

        // Retrieve the player's bounty progress
        AccessDeniedModVariables.PlayerVariables playerVariables = player.getData(AccessDeniedModVariables.PLAYER_VARIABLES);
        if (playerVariables == null) {
            return 0;
        }

        Map<String, Integer> bountyProgress = playerVariables.getBountyProgress();
        if (bountyProgress == null) {
            return 0;
        }

        // Return the kill count for the specified mob
        return bountyProgress.getOrDefault(mob, 0);
    }
}





