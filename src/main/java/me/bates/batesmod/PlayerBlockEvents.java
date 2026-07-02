package me.bates.batesmod;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionResult;

public class PlayerBlockEvents {
    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, _, _) -> {
            if (level.isClientSide()) {
                return true;
            }

            for (Region r : ConfigManager.get().protectedRegions) {
                if (r.isInRegion(pos.getX(),  pos.getY(), pos.getZ())) {
                    if (player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
                        player.sendSystemMessage(TextTools.builder().input(ConfigManager.get().protectedRegionMessageAdmin)
                                .placeholder("bates", ConfigManager.get().batesModGradient)
                                .placeholder("region-name", r.getName()).build());
                        return true;
                    }

                    player.sendSystemMessage(TextTools.builder().input(ConfigManager.get().protectedRegionMessage)
                            .placeholder("bates", ConfigManager.get().batesModGradient)
                            .placeholder("region-name", r.getName()).build());
                    return false;
                }
            }
            return true;
        });

        UseBlockCallback.EVENT.register((player, level, _, blockHitResult) -> {
            if (level.isClientSide()) {
                return InteractionResult.PASS;
            }

            BlockPos pos = blockHitResult.getBlockPos().offset(blockHitResult.getDirection().getUnitVec3i());
            for (Region r : ConfigManager.get().protectedRegions) {
                if (r.isInRegion(pos.getX(),  pos.getY(), pos.getZ())) {
                    if (player.permissions().hasPermission(Permissions.COMMANDS_ADMIN)) {
                        player.sendSystemMessage(TextTools.builder().input(ConfigManager.get().protectedRegionMessageAdmin)
                                .placeholder("bates", ConfigManager.get().batesModGradient)
                                .placeholder("region-name", r.getName()).build());
                        return InteractionResult.PASS;
                    }

                    player.sendSystemMessage(TextTools.builder().input(ConfigManager.get().protectedRegionMessage)
                            .placeholder("bates", ConfigManager.get().batesModGradient)
                            .placeholder("region-name", r.getName()).build());
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });
    }
}
