package fox.mods.accessdenied;

import fox.mods.accessdenied.configuration.ADClientFileConfiguration;
import fox.mods.accessdenied.util.DimensionUtils;
import fox.mods.accessdenied.util.PortalUtils;
import net.minecraft.ChatFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;


@Mod.EventBusSubscriber(modid = AccessDenied.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityTravelsToDimension {
    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        execute(event, event.getDimension().location().toString(), event.getEntity());
    }

    public static void execute(String dimension, Entity entity) {
        execute(null, dimension, entity);
    }

    private static void execute(@Nullable Event event, String dimension, Entity entity) {
        if (dimension == null || entity == null)
            return;

        Pair<String, String> dimensionInfo = DimensionUtils.getDimensionNamespaceAndPath(dimension);
        String dimensionNamespace = dimensionInfo.getLeft();
        String dimensionPath = dimensionInfo.getRight();

        List<Pair<String, String>> enabledDimensions = DimensionUtils.getDisabledDimensions();
        boolean isDisabled = enabledDimensions.stream()
                .anyMatch(dim -> dim.getLeft().equals(dimensionNamespace) && dim.getRight().equals(dimensionPath));

        if (isDisabled) {
            if (event != null && event.isCancelable()) {
                event.setCanceled(true);
                if (entity instanceof Player _player && !_player.level().isClientSide()) {
                    String translationKey = "dimension." + dimensionNamespace + "." + dimensionPath;
                    if(ADClientFileConfiguration.SHOW_WARNING.get()) {
                        _player.displayClientMessage(Component.translatable("accessdenied.warning.text", Component.translatable(translationKey)).withStyle(ChatFormatting.RED), true);
                    }
                    // Check if the player is standing in a portal block
                    if (PortalUtils.isPlayerInPortal(_player)) {
                        PortalUtils.teleportPlayerOutsidePortal(_player);
                    }
                }
            }
        }
    }
}


