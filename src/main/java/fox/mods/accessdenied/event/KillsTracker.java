package fox.mods.accessdenied.event;

import fox.mods.accessdenied.AccessDenied;
import fox.mods.accessdenied.network.AccessDeniedModVariables;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = AccessDenied.ID, bus = EventBusSubscriber.Bus.GAME)
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
            AccessDeniedModVariables.PlayerVariables _vars = sourceentity.getData(AccessDeniedModVariables.PLAYER_VARIABLES);
            AccessDeniedModVariables.PlayerVariables updatedVars = new AccessDeniedModVariables.PlayerVariables(
                    _vars.getKills() + 1,
                    _vars.getBlocksMined(),
                    _vars.getBlocksPlaced(),
                    _vars.getBountyProgress()
            );
            sourceentity.setData(AccessDeniedModVariables.PLAYER_VARIABLES, updatedVars);
            updatedVars.syncPlayerVariables(sourceentity);
        }
    }
}

