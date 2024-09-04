package fox.mods.nodimensions;

import fox.mods.api.nodimensions.configuration.NoDimensionsFileConfiguration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;


@Mod.EventBusSubscriber
public class EntityTravelsToDimension {
    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        execute(event, event.getDimension(), event.getEntity());
    }

    public static void execute(ResourceKey<Level> dimension, Entity entity) {
        execute(null, dimension, entity);
    }

    private static void execute(@Nullable Event event, ResourceKey<Level> dimension, Entity entity) {
        if (dimension == null || entity == null)
            return;
        if (dimension == Level.NETHER) {
            if (NoDimensionsFileConfiguration.NETHER_ENABLED.get() == false) {
                if (event != null && event.isCancelable()) {
                    event.setCanceled(true);
                } else if (event != null && event.hasResult()) {
                    event.setResult(Event.Result.DENY);
                }
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(Component.literal("§c§lYou cannot travel to the Nether..."), true);
            }
        } else if (dimension == Level.END) {
            if (NoDimensionsFileConfiguration.END_ENABLED.get() == false) {
                if (event != null && event.isCancelable()) {
                    event.setCanceled(true);
                } else if (event != null && event.hasResult()) {
                    event.setResult(Event.Result.DENY);
                }
                if (entity instanceof Player _player && !_player.level().isClientSide())
                    _player.displayClientMessage(Component.literal("§c§lYou cannot travel to the End..."), true);
            }
        }
    }
}

