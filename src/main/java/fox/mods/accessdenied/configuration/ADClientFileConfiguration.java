package fox.mods.accessdenied.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class ADClientFileConfiguration {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SHOW_WARNING;

    static {
        BUILDER.push("General Settings");
        SHOW_WARNING = BUILDER.define("showWarning", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}