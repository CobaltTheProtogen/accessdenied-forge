package fox.mods.accessdenied.event;

import fox.mods.accessdenied.AccessDenied;
import fox.mods.accessdenied.network.AccessDeniedModVariables;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = AccessDenied.ID, bus = EventBusSubscriber.Bus.GAME)
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
            AccessDeniedModVariables.PlayerVariables _vars = entity.getData(AccessDeniedModVariables.PLAYER_VARIABLES);
            AccessDeniedModVariables.PlayerVariables updatedVars = new AccessDeniedModVariables.PlayerVariables(
                    _vars.getKills(),
                    _vars.getBlocksMined() + 1,
                    _vars.getBlocksPlaced(),
                    _vars.getBountyProgress()
            );
            entity.setData(AccessDeniedModVariables.PLAYER_VARIABLES, updatedVars);
            _vars.syncPlayerVariables(entity);
        }
    }
}


