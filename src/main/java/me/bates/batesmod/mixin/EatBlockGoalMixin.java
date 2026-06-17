package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EatBlockGoal.class)
public abstract class EatBlockGoalMixin {


    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    boolean bates$cancelDestroyBlockCall(Level instance, BlockPos blockPos, boolean b) {
        if (ModGameRules.isMobGriefEnabled(EntityType.SHEEP, (ServerLevel) instance)) {
            return instance.destroyBlock(blockPos, b);
        }
        return false;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    void bates$cancelLevelEvent(Level instance, int i, BlockPos blockPos, int j) {
        if (ModGameRules.isMobGriefEnabled(EntityType.SHEEP, (ServerLevel) instance)) {
            instance.levelEvent(i, blockPos, j);
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    boolean bates$cancelSetBlock(Level instance, BlockPos pos, BlockState blockState, int updateFlags) {
        if (ModGameRules.isMobGriefEnabled(EntityType.SHEEP, (ServerLevel) instance)) {
            instance.setBlock(pos, blockState, updateFlags);
        }
        return false;
    }
}
