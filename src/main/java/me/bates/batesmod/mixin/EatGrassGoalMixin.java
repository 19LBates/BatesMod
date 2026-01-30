package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EatGrassGoal.class)
public abstract class EatGrassGoalMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    boolean bates$cancelBreakBlockCall(World instance, BlockPos blockPos, boolean b) {
        ServerWorld sw = (ServerWorld) instance;
        if (ModGameRules.isMobGriefEnabled(EntityType.SHEEP, sw)) {
            return instance.breakBlock(blockPos, b);
        }
        return false;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    boolean bates$cancelChangeBlockCall(World instance, BlockPos blockPos, BlockState state, int flags) {
        ServerWorld sw = (ServerWorld) instance;
        if (ModGameRules.isMobGriefEnabled(EntityType.SHEEP, sw)) {
            return instance.setBlockState(blockPos, state, flags);
        }
        return false;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V"))
    void bates$cancelSyncWorldEvent(World instance, int eventID, BlockPos blockPos, int data) {
        ServerWorld sw = (ServerWorld) instance;
        if (ModGameRules.isMobGriefEnabled(EntityType.SHEEP, sw)) {
            instance.syncWorldEvent(eventID, blockPos, data);
        }
    }
}
