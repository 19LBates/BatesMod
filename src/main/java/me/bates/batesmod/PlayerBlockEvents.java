package me.bates.batesmod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionResult;

public class PlayerBlockEvents {
    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((_, player, pos, _, _) -> {
            for (Region r : ConfigManager.get().protectedRegions) {
                if (r.isInRegion(pos.getX(),  pos.getY(), pos.getZ())) {
                    player.sendSystemMessage(TextTools.builder().input(ConfigManager.get().protectedRegionMessage)
                            .placeholder("bates", ConfigManager.get().batesModGradient).build());
                    return player.permissions().hasPermission(Permissions.COMMANDS_ADMIN);
                }
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, _, _, blockHitResult) -> {
            BlockPos pos = blockHitResult.getBlockPos().offset(blockHitResult.getDirection().getUnitVec3i());
            for (Region r : ConfigManager.get().protectedRegions) {
                if (r.isInRegion(pos.getX(),  pos.getY(), pos.getZ())) {
                    player.sendSystemMessage(TextTools.builder().input(ConfigManager.get().protectedRegionMessage)
                            .placeholder("bates", ConfigManager.get().batesModGradient).build());
                    if (player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) return InteractionResult.PASS;
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });
    }
}
