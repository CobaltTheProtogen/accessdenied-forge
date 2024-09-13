package fox.mods.accessdenied;

import fox.mods.accessdenied.configuration.ADClientFileConfiguration;
import fox.mods.accessdenied.configuration.ADFileConfiguration;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;

@Mod(AccessDenied.ID)
public class AccessDenied {
    public static final Logger LOGGER = LogManager.getLogger(AccessDenied.class);
    public static final String ID = "accessdenied";
    public static final String NAME = "Access Denied";
    public static final String VERSION = "4.0.1";

    public AccessDenied() {
        MinecraftForge.EVENT_BUS.register(this);
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ADClientFileConfiguration.SPEC, "foxmods/client/accessdenied.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ADFileConfiguration.SPEC, "foxmods/accessdenied.toml");
    }
}




