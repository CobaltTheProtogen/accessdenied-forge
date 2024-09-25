package fox.mods.accessdenied.network;

import fox.mods.accessdenied.AccessDenied;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.common.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AccessDeniedModVariables {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, AccessDenied.ID);
	public static final Supplier<AttachmentType<PlayerVariables>> PLAYER_VARIABLES = ATTACHMENT_TYPES.register("player_variables", () -> AttachmentType.serializable(() -> new PlayerVariables(
			0,
			0,
			0,
			new HashMap<>()
	)).build());

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		AccessDenied.addNetworkMessage(PlayerVariablesSyncMessage.TYPE, PlayerVariablesSyncMessage.STREAM_CODEC, PlayerVariablesSyncMessage::handleData);
	}

	@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
	public static class EventBusVariableHandlers {
		@SubscribeEvent
		public static void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
			if (event.getEntity() instanceof ServerPlayer player)
				player.getData(PLAYER_VARIABLES).syncPlayerVariables(event.getEntity());
		}
		@SubscribeEvent
		public static void onPlayerLoggedOutSyncPlayerVariables(PlayerEvent.PlayerLoggedOutEvent event) {
			if (event.getEntity() instanceof ServerPlayer player)
				player.getData(PLAYER_VARIABLES).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
			if (event.getEntity() instanceof ServerPlayer player)
				player.getData(PLAYER_VARIABLES).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
			if (event.getEntity() instanceof ServerPlayer player)
				player.getData(PLAYER_VARIABLES).syncPlayerVariables(event.getEntity());
		}

		@SubscribeEvent
		public static void clonePlayer(PlayerEvent.Clone event) {
			PlayerVariables original = event.getOriginal().getData(PLAYER_VARIABLES);
			PlayerVariables clone = new PlayerVariables(
					original.getKills(),
					original.getBlocksMined(),
					original.getBlocksPlaced(),
					new HashMap<>(original.getBountyProgress())
			);
			event.getEntity().setData(PLAYER_VARIABLES, clone);
		}
	}

	public static class PlayerVariables implements INBTSerializable<CompoundTag> {
		private int kills;
		private int blocksMined;
		private int blocksPlaced;
		private Map<String, Integer> bountyProgress;

		public PlayerVariables(int kills, int blocksMined, int blocksPlaced, Map<String, Integer> bountyProgress) {
			this.kills = kills;
			this.blocksMined = blocksMined;
			this.blocksPlaced = blocksPlaced;
			this.bountyProgress = bountyProgress;
		}

		@Override
		public CompoundTag serializeNBT(HolderLookup.Provider lookupProvider) {
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("kills", kills);
			nbt.putInt("blocksMined", blocksMined);
			nbt.putInt("blocksPlaced", blocksPlaced);
			CompoundTag bountyTag = new CompoundTag();
			bountyProgress.forEach(bountyTag::putInt);
			nbt.put("bountyProgress", bountyTag);
			return nbt;
		}

		@Override
		public void deserializeNBT(HolderLookup.Provider lookupProvider, CompoundTag nbt) {
			kills = nbt.getInt("kills");
			blocksMined = nbt.getInt("blocksMined");
			blocksPlaced = nbt.getInt("blocksPlaced");

			CompoundTag bountyTag = nbt.getCompound("bountyProgress");
			bountyProgress = new HashMap<>();
			bountyTag.getAllKeys().forEach(key -> bountyProgress.put(key, bountyTag.getInt(key)));
		}

		public void syncPlayerVariables(Entity entity) {
			if (entity instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new PlayerVariablesSyncMessage(this));
			}
		}

		public void updateBountyProgress(String mob, int count) {
			bountyProgress.put(mob, bountyProgress.getOrDefault(mob, 0) + count);
		}

		public int getKills() {
			return kills;
		}

		public int getBlocksMined() {
			return blocksMined;
		}

		public int getBlocksPlaced() {
			return blocksPlaced;
		}

		public Map<String, Integer> getBountyProgress() {
			return bountyProgress;
		}
	}

	public static class PlayerVariablesSyncMessage implements CustomPacketPayload {
		private final PlayerVariables data;

		public static final Type<PlayerVariablesSyncMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AccessDenied.ID, "player_variables_sync"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PlayerVariablesSyncMessage> STREAM_CODEC = StreamCodec
				.of((RegistryFriendlyByteBuf buffer, PlayerVariablesSyncMessage message) -> buffer.writeNbt(message.data.serializeNBT(buffer.registryAccess())), (RegistryFriendlyByteBuf buffer) -> {
					PlayerVariables playerVariables = new PlayerVariables(
							0,
							0,
							0,
							new HashMap<>()
					);
					playerVariables.deserializeNBT(buffer.registryAccess(), buffer.readNbt());
					return new PlayerVariablesSyncMessage(playerVariables);
				});

		public PlayerVariablesSyncMessage(PlayerVariables data) {
			this.data = data;
		}

		@Override
		public Type<PlayerVariablesSyncMessage> type() {
			return TYPE;
		}

		public static void handleData(final PlayerVariablesSyncMessage message, final IPayloadContext context) {
			if (context.flow() == PacketFlow.CLIENTBOUND && message.data != null) {
				context.enqueueWork(() -> context.player().getData(PLAYER_VARIABLES).deserializeNBT(context.player().registryAccess(), message.data.serializeNBT(context.player().registryAccess()))).exceptionally(e -> {
					context.connection().disconnect(Component.literal(e.getMessage()));
					return null;
				});
			}
		}
	}
}
