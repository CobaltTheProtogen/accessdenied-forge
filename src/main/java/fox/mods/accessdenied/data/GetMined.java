package fox.mods.accessdenied.data;


import fox.mods.accessdenied.network.AccessDeniedModVariables;
import net.minecraft.world.entity.Entity;

public class GetMined {
    public static int execute(Entity entity) {
        if (entity == null)
            return 0;
        return (entity.getCapability(AccessDeniedModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new AccessDeniedModVariables.PlayerVariables())).blocksMined;
    }
}

