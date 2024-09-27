package fox.mods.accessdenied.event;

import fox.mods.accessdenied.AccessDenied;
import fox.mods.accessdenied.network.AccessDeniedModVariables;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class MiningTracker {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().isSolid()) {
            execute(event, event.getPlayer());
        }
    }

    public static void execute(Entity entity) {
        execute(null, entity);
    }

    private static void execute(@Nullable Event event, Entity entity) {
        if (entity == null)
            return;
        {
            int _setval = (entity.getCapability(AccessDeniedModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new AccessDeniedModVariables.PlayerVariables())).blocksMined + 1;
            entity.getCapability(AccessDeniedModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
                capability.kills = _setval;
                capability.syncPlayerVariables(entity);
            });
        }
    }
}



