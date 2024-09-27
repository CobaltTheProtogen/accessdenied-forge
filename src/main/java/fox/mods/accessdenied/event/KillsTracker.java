package fox.mods.accessdenied.event;

import fox.mods.accessdenied.AccessDenied;
import fox.mods.accessdenied.network.AccessDeniedModVariables;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class KillsTracker {
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() != null) {
            execute(event, event.getSource().getEntity());
        }
    }

    public static void execute(Entity sourceentity) {
        execute(null, sourceentity);
    }

    private static void execute(@Nullable Event event, Entity sourceentity) {
        if (sourceentity == null)
            return;
        if (sourceentity instanceof Player) {
            int _setval = (sourceentity.getCapability(AccessDeniedModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new AccessDeniedModVariables.PlayerVariables())).kills + 1;
            sourceentity.getCapability(AccessDeniedModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.kills = _setval;
                capability.syncPlayerVariables(sourceentity);
            });
        }
    }
}


