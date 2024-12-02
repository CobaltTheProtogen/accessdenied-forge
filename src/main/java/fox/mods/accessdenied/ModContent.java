package fox.mods.accessdenied;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModContent {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AccessDenied.ID);

    public static final DeferredItem<Item> DUMMY_PORTAL = ITEMS.registerSimpleItem("dummy_portal");


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);

    }
}
