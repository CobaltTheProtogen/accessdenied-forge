package fox.mods.accessdenied.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PortalUtils {
    public static boolean isPlayerInPortal(Player player) {
        BlockPos playerPos = player.blockPosition();
        Level level = player.level();

        // Check a 3x3x3 area around the player for portal blocks
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = playerPos.offset(x, y, z);
                    BlockState blockState = level.getBlockState(checkPos);
                    if (blockState.is(BlockTags.PORTALS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void teleportPlayerOutsidePortal(Player player) {
        // Teleport the player to a safe position 3 blocks back along the direction they are facing
        Vec3 lookVec = player.getLookAngle();
        Vec3 teleportVec = player.position().subtract(lookVec.scale(3));

        // Ensure the teleport position is safe
        BlockPos teleportPos = new BlockPos((int) teleportVec.x, (int) teleportVec.y, (int) teleportVec.z);
        Level level = player.level();
        BlockState blockState = level.getBlockState(teleportPos);

        // Check if the teleport position is not obstructed
        if (!blockState.isSolidRender(level, teleportPos)) {
            player.teleportTo(teleportVec.x, teleportVec.y, teleportVec.z);
        } else {
            // Find a nearby safe position
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos checkPos = teleportPos.offset(x, y, z);
                        BlockState checkState = level.getBlockState(checkPos);
                        if (!checkState.isSolidRender(level, checkPos)) {
                            player.teleportTo(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                            return;
                        }
                    }
                }
            }
        }
    }
}
