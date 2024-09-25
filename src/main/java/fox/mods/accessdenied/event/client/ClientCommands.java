package fox.mods.accessdenied.event.client;

import com.mojang.brigadier.CommandDispatcher;
import fox.mods.accessdenied.AccessDenied;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = AccessDenied.ID, bus = EventBusSubscriber.Bus.GAME)
public class ClientCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal(AccessDenied.ID)
                .then(Commands.literal("version")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            context.getSource().sendSystemMessage(Component.translatable("accessdenied.message.version.text", AccessDenied.NAME, AccessDenied.VERSION));
                            return 1;
                        })));
    }
}