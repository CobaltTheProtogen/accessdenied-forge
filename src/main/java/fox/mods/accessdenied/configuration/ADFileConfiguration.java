package fox.mods.accessdenied.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ADFileConfiguration {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSIONS_DISABLED;

    static {
        BUILDER.push("General Settings");
        DIMENSIONS_DISABLED = BUILDER.comment("Defines a list of dimensions that are disabled. If a dimension is not in this list, it will be enabled by default.").defineListAllowEmpty(List.of("dimensionsDisabled"), () -> Arrays.asList("minecraft:the_nether", "minecraft:the_end"), e -> e instanceof String && ((String) e).contains(":"));
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static List<? extends String> getDimensionsDisabled() {
        return DIMENSIONS_DISABLED.get();
    }
}