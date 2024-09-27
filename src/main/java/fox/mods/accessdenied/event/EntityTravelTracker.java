package fox.mods.accessdenied.event;

import fox.mods.accessdenied.AccessDenied;
import fox.mods.accessdenied.access.AccessManager;
import fox.mods.accessdenied.access.criteria.*;
import fox.mods.accessdenied.data.*;
import fox.mods.accessdenied.data.json.DimensionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber
public class EntityTravelTracker {
    private static final AccessManager accessManager = new AccessManager();
    private static final Random random = new Random();

    static {
        accessManager.addCriteria(new KillAccess());
        accessManager.addCriteria(new MineAccess());
        accessManager.addCriteria(new PlaceAccess());
        accessManager.addCriteria(new BountyAccess());
        accessManager.addCriteria(new ItemAccess());
        accessManager.addCriteria(new AdvancementAccess());
        accessManager.addCriteria(new LevelAccess());
    }

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

        boolean teleportationSuccessful = true;

        // Retrieve the DimensionData for the given dimension
        DimensionManager.DimensionData dimensionData = DimensionManager.getDimensionData().get(dimension);

        // Check if the dimension is disabled
        if (dimensionData != null && dimensionData.disabled().orElse(false)) {
            if (event instanceof ICancellableEvent _cancellable) {
                _cancellable.setCanceled(true);
                teleportationSuccessful = false;
                if (entity instanceof Player player && !player.level().isClientSide()) {
                    if(dimension == Level.END) {
                        destroyEndPortal(player);
                    }
                    else {
                        destroyPortal(player);
                    }
                    if (dimensionData != null && dimensionData.messages().isPresent()) {
                        List<String> messageList = dimensionData.messages().get();
                        if (!messageList.isEmpty()) {
                            String randomMessage = messageList.get(random.nextInt(messageList.size()));
                            if (!randomMessage.isEmpty()) {
                                player.displayClientMessage(Component.literal(randomMessage), true);
                            }
                        }
                    } else {
                        player.displayClientMessage(Component.translatable("accessdenied.warning.text"), true);
                    }
                }
                return;
            }
        }

        if (entity instanceof Player player) {
            if (!accessManager.canEnterDimension(player, dimension)) {
                if (event instanceof ICancellableEvent _cancellable) {
                    _cancellable.setCanceled(true);
                    teleportationSuccessful = false;
                    if (!player.level().isClientSide()) {
                        if(dimension == Level.END) {
                            destroyEndPortal(player);
                        }
                        else {
                            destroyPortal(player);
                        }
                        // Get the criteria the player needs to meet
                        List<Component> criteriaMessages = getCriteriaMessages(player, dimension);
                        List<Component> messages = new ArrayList<>();
                        if (dimensionData != null) {
                            if (dimensionData.messages().isPresent()) {
                                List<String> messageList = dimensionData.messages().get();
                                if (!messageList.isEmpty()) {
                                    String randomMessage = messageList.get(random.nextInt(messageList.size()));
                                    if (!randomMessage.isEmpty()) {
                                        messages.add(Component.empty()
                                                .append(Component.literal(randomMessage))
                                        );
                                    }
                                }
                            } else {
                                messages.add(Component.empty()
                                        .append(Component.translatable("accessdenied.warning.text"))
                                );
                            }
                        }
                        for (Component message : messages) {
                            player.displayClientMessage(message, true);
                        }
                        for (Component criteriaMessage : criteriaMessages) {
                            player.displayClientMessage(criteriaMessage, false);
                        }
                    }
                }
            }
        }
        // If teleportation was successful, handle item consumption
        if (teleportationSuccessful && entity instanceof Player player) {
            accessManager.handleItemConsumption(player, dimension);
        }
    }

    private static void destroyPortal(Player player) {
        BlockPos playerPos = player.blockPosition();
        Level level = player.level();

        // Check a 10x10x10 area around the player for portal blocks
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);
                    ResourceLocation location = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
                    if (location.getPath().contains("portal") || blockState.is(BlockTags.PORTALS)) {
                        level.destroyBlock(checkPos, false);
                        if (level instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.END_ROD, checkPos.getX(), (checkPos.getY() + 0.5), checkPos.getZ(), 90, 0.1, 0.1, 0.1, 1);
                        return;
                    }
                }
            }
        }
    }

    private static void destroyEndPortal(Player player) {
        BlockPos playerPos = player.blockPosition();
        Level level = player.level();

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);

                    if (blockState.is(BlockTags.PORTALS)) {
                        level.destroyBlock(checkPos, false);
                    }
                }
            }
        }

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);

                    if (blockState.is(Blocks.END_PORTAL_FRAME) && blockState.getValue(EndPortalFrameBlock.HAS_EYE)) {
                        BlockState newState = Blocks.END_PORTAL_FRAME.defaultBlockState()
                                .setValue(EndPortalFrameBlock.FACING, blockState.getValue(EndPortalFrameBlock.FACING))
                                .setValue(EndPortalFrameBlock.HAS_EYE, false);
                        level.setBlock(checkPos, newState, 3);

                        if (level instanceof ServerLevel _level)
                            _level.sendParticles(ParticleTypes.END_ROD, checkPos.getX(), (checkPos.getY() + 0.5), checkPos.getZ(), 90, 0.1, 0.1, 0.1, 1);

                        ItemEntity eyeOfEnder = new ItemEntity(level, checkPos.getX() + 0.5, checkPos.getY() + 1.0, checkPos.getZ() + 0.5,
                                new ItemStack(Items.ENDER_EYE));

                        eyeOfEnder.setDeltaMovement(0.0, 0.0, 0.0);
                        eyeOfEnder.setNoGravity(true);

                        level.addFreshEntity(eyeOfEnder);

                        return;
                    }
                }
            }
        }
    }



    public static List<Component> getCriteriaMessages(Player player, ResourceKey<Level> dimension) {
        List<Component> messages = new ArrayList<>();
        KillAccess killAccess = new KillAccess();
        MineAccess blockAccess = new MineAccess();
        PlaceAccess placeAccess = new PlaceAccess();
        BountyAccess bountyAccess = new BountyAccess();
        ItemAccess itemAccess = new ItemAccess();
        AdvancementAccess advancementAccess = new AdvancementAccess();
        LevelAccess levelAccess = new LevelAccess();

        Integer killCriteria = killAccess.getKillCriteria().get(dimension);
        Integer mineCriteria = blockAccess.getMineCriteria().get(dimension);
        Integer placementCriteria = placeAccess.getPlacementCriteria().get(dimension);
        List<DimensionManager.Bounty> bountyCriteria = bountyAccess.getBountyCriteria().get(dimension);
        List<DimensionManager.ItemRequirement> itemCriteria = itemAccess.getItemCriteria().get(dimension);
        List<ResourceKey<Advancement>> advancementCriteria = advancementAccess.getAdvancementCriteria().get(dimension);
        Integer levelCriteria = levelAccess.getLevelCriteria().get(dimension);

        if (killCriteria != null && killCriteria > 0) {
            int killed = GetKills.execute(player);
            boolean criteriaMet = killed >= killCriteria;
            messages.add(Component.empty()
                    .append(Component.literal(criteriaMet ? "✔ " : "✘ ").withStyle(criteriaMet ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(Component.translatable("accessdenied.requirement.kills.text").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(killed + "/" + killCriteria).withStyle(ChatFormatting.GRAY))
            );
        }
        if (mineCriteria != null && mineCriteria > 0) {
            int mined = GetMined.execute(player);
            boolean criteriaMet = mined >= mineCriteria;
            messages.add(Component.empty()
                    .append(Component.literal(criteriaMet ? "✔ " : "✘ ").withStyle(criteriaMet ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(Component.translatable("accessdenied.requirement.mined.text").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(mined + "/" + mineCriteria).withStyle(ChatFormatting.GRAY))
            );
        }
        if (placementCriteria != null && placementCriteria > 0) {
            int placed = GetPlaced.execute(player);
            boolean criteriaMet = placed >= placementCriteria;
            messages.add(Component.empty()
                    .append(Component.literal(criteriaMet ? "✔ " : "✘ ").withStyle(criteriaMet ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(Component.translatable("accessdenied.requirement.placed.text").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(placed + "/" + placementCriteria).withStyle(ChatFormatting.GRAY))
            );
        }
        if (bountyCriteria != null && !bountyCriteria.isEmpty()) {
            for (DimensionManager.Bounty bounty : bountyCriteria) {
                int trophies = GetBounty.execute(player, bounty.mob());
                boolean criteriaMet = trophies >= bounty.count();
                ResourceLocation entityRegistryName = ResourceLocation.tryParse(bounty.mob());
                String translatableEntityName = BuiltInRegistries.ENTITY_TYPE.get(entityRegistryName).toString();
                messages.add(
                        Component.empty()
                                .append(Component.literal(criteriaMet ? "✔ " : "✘ ").withStyle(criteriaMet ? ChatFormatting.GREEN : ChatFormatting.RED))
                                .append(Component.translatable(translatableEntityName).withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(" ").withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable("accessdenied.requirement.bounty.text").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(trophies + "/" + bounty.count()).withStyle(ChatFormatting.GRAY))
                );
            }
        }
        if (advancementCriteria != null && !advancementCriteria.isEmpty()) {
            if (player instanceof ServerPlayer serverPlayer)
                for (ResourceKey<Advancement> advancementKey : advancementCriteria) {
                    Advancement advancement = Objects.requireNonNull(serverPlayer.server.getAdvancements().get(advancementKey.location())).value();
                    if (advancement != null) {
                        AdvancementProgress progress = serverPlayer.getAdvancements().getOrStartProgress(new AdvancementHolder(advancementKey.location(), advancement));
                        boolean criteriaMet = progress.isDone();
                        String advancementName = advancement.display().get().getTitle().getString();
                        messages.add(
                                Component.empty()
                                        .append(Component.literal(criteriaMet ? "✔ " : "✘ ").withStyle(criteriaMet ? ChatFormatting.GREEN : ChatFormatting.RED))
                                        .append(Component.translatable("accessdenied.requirement.advancement.text")).withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(": ")).withStyle(ChatFormatting.GRAY)
                                        .append(Component.literal(advancementName).withStyle(ChatFormatting.GRAY))
                        );
                    }
                }
        }
        if (levelCriteria != null && levelCriteria > 0) {
            int playerLevel = player.experienceLevel;
            boolean criteriaMet = playerLevel >= levelCriteria;
            messages.add(Component.empty()
                    .append(Component.literal(criteriaMet ? "✔ " : "✘ ").withStyle(criteriaMet ? ChatFormatting.GREEN : ChatFormatting.RED))
                    .append(Component.translatable("accessdenied.requirement.levels.text").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(playerLevel + "/" + levelCriteria).withStyle(ChatFormatting.GRAY))
            );
        }
        if (itemCriteria != null && !itemCriteria.isEmpty()) {
            for (DimensionManager.ItemRequirement items : itemCriteria) {
                int keys = GetItem.execute(player, items.item() != null ? items.item().location() : null, items.tag());
                boolean criteriaMet = keys >= items.count();

                MutableComponent criteriaMessage = Component.literal(criteriaMet ? "✔ " : "✘ ")
                        .withStyle(criteriaMet ? ChatFormatting.GREEN : ChatFormatting.RED);
                if (items.item() != null) {
                    ResourceLocation itemRegistryName = items.item().location();
                    criteriaMessage.append(Component.translatable(itemRegistryName.toLanguageKey("item")).withStyle(ChatFormatting.GRAY));
                } else if (items.tag() != null) {
                    ResourceLocation tagRegistryName = items.tag().location();
                    criteriaMessage.append(Component.literal("#" + tagRegistryName).withStyle(ChatFormatting.GRAY));
                } else {
                    criteriaMessage.append(Component.literal("Unknown Item").withStyle(ChatFormatting.RED));
                }
                messages.add(
                        criteriaMessage
                                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal(keys + "/" + items.count()).withStyle(ChatFormatting.GRAY))
                );
            }
        }
        return messages;
    }
}