package fox.mods.nodimensions;

import fox.mods.api.util.DimensionUtils;
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


@Mod.EventBusSubscriber(modid = NoDimensionsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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

        List<Pair<String, String>> allDimensions = DimensionUtils.getDisabledDimensions();
        boolean isDisabled = allDimensions.stream()
                .anyMatch(dim -> dim.getLeft().equals(dimensionNamespace) && dim.getRight().equals(dimensionPath));

        if (isDisabled) {
            if (event != null && event.isCancelable()) {
                event.setCanceled(true);
            } else if (event != null && event.hasResult()) {
                event.setResult(Event.Result.DENY);
            }
            if (entity instanceof Player _player && !_player.level().isClientSide())
                _player.displayClientMessage(Component.literal("§cYou cannot travel to this dimension..."), true);
        }
    }
}


