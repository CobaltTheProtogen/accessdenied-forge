package fox.mods.accessdenied.data.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fox.mods.accessdenied.AccessDenied;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DimensionManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    private static final String DIRECTORY = "dimensions";
    private static final DimensionManager INSTANCE = new DimensionManager();
    private static final HashMap<ResourceKey<Level>, DimensionData> criteria = new HashMap<>();

    public DimensionManager() {
        super(GSON, DIRECTORY);
    }

    public record DimensionData(
            ResourceKey<Level> dimension,
            Optional<Boolean> disabled,
            Optional<List<String>> messages,
            Optional<CriteriaData> criteria) {

        public static final Codec<DimensionData> DIMENSION_DATA = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionData::dimension),
                Codec.BOOL.optionalFieldOf("disabled").forGetter(DimensionData::disabled),
                Codec.STRING.listOf().optionalFieldOf("messages").forGetter(DimensionData::messages),
                CriteriaData.CRITERIA_DATA.optionalFieldOf("requirements").forGetter(DimensionData::criteria)
        ).apply(instance, DimensionData::new));
    }

    public record CriteriaData(
            Optional<List<Bounty>> bounties,
            Optional<List<ResourceKey<Advancement>>> advancements,
            Optional<List<ItemRequirement>> items,
            Optional<Integer> mobs,
            Optional<Integer> level,
            Optional<Integer> blocksMined,
            Optional<Integer> blocksPlaced) {

        public static final Codec<CriteriaData> CRITERIA_DATA = RecordCodecBuilder.create(instance -> instance.group(
                Bounty.BOUNTY_CODEC.listOf().optionalFieldOf("bounties").forGetter(CriteriaData::bounties),
                ResourceKey.codec(Registries.ADVANCEMENT).listOf().optionalFieldOf("advancements").forGetter(CriteriaData::advancements),
                ItemRequirement.ITEM_REQUIREMENT_CODEC.listOf().optionalFieldOf("items").forGetter(CriteriaData::items),
                Codec.INT.optionalFieldOf("mobs_killed").forGetter(CriteriaData::mobs),
                Codec.INT.optionalFieldOf("level").forGetter(CriteriaData::level),
                Codec.INT.optionalFieldOf("blocks_mined").forGetter(CriteriaData::blocksMined),
                Codec.INT.optionalFieldOf("blocks_placed").forGetter(CriteriaData::blocksPlaced)
        ).apply(instance, CriteriaData::new));
    }

    public record Bounty(
            String mob,
            int count) {

        public static final Codec<Bounty> BOUNTY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("mob").forGetter(Bounty::mob),
                Codec.INT.fieldOf("count").forGetter(Bounty::count)
        ).apply(instance, Bounty::new));
    }

    public record ItemRequirement(
            @Nullable ResourceKey<Item> item,
            @Nullable TagKey<Item> tag,
            int count,
            Optional<Integer> consume) {

        public static final Codec<ItemRequirement> ITEM_REQUIREMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.ITEM).optionalFieldOf("item").forGetter(requirement -> Optional.ofNullable(requirement.item)),
                TagKey.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(requirement -> Optional.ofNullable(requirement.tag)),
                Codec.INT.fieldOf("count").forGetter(ItemRequirement::count),
                Codec.INT.optionalFieldOf("consume").forGetter(ItemRequirement::consume)
        ).apply(instance, (itemOpt, tagOpt, count, consume) -> new ItemRequirement(itemOpt.orElse(null), tagOpt.orElse(null), count, consume)));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        criteria.clear();
        resources.forEach((resourceLocation, jsonElement) -> {
            try {
                DataResult<DimensionData> dataResult = DimensionData.DIMENSION_DATA.parse(JsonOps.INSTANCE, jsonElement);
                dataResult.resultOrPartial(result -> {
                    System.out.println("Parsing dimension data for: " + resourceLocation + " with result: " + result);
                }).ifPresent(this::addToCriteria);
            } catch (Exception e) {
                AccessDenied.LOGGER.error("Couldn't parse dimension requirements file {}", resourceLocation, e);
            }
        });
    }

    private void addToCriteria(DimensionData data) {
        criteria.put(data.dimension(), data);
    }

    public static HashMap<ResourceKey<Level>, DimensionData> getDimensionData() {
        return criteria;
    }

    public static DimensionManager getInstance() {
        return INSTANCE;
    }
}











