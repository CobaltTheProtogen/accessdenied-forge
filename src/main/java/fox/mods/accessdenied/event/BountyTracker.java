package fox.mods.accessdenied.event;

import fox.mods.accessdenied.AccessDenied;
import fox.mods.accessdenied.network.AccessDeniedModVariables;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = AccessDenied.ID, bus = EventBusSubscriber.Bus.GAME)
public class BountyTracker {
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (event.getEntity() != null && event.getSource().getEntity() instanceof Player) {
            execute(event, (Player) event.getSource().getEntity(), event.getEntity());
        }
    }

    public static void execute(Player player, Entity targetentity) {
        execute(null, player, targetentity);
    }

    private static void execute(@Nullable Event event, Player player, Entity targetentity) {
        if (player == null || targetentity == null) {
            System.out.println("BountyTracker: Player or target entity is null.");
            return;
        }

        AccessDeniedModVariables.PlayerVariables _vars = player.getData(AccessDeniedModVariables.PLAYER_VARIABLES);
        String mobName = BuiltInRegistries.ENTITY_TYPE.getKey(targetentity.getType()).toString();
        _vars.updateBountyProgress(mobName, 1);
        _vars.syncPlayerVariables(player);
    }
}


