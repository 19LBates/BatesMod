package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(method = "createWitherRose", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean bates$stopWitherRose(Level instance, BlockPos pos, BlockState blockState, int updateFlags) {
        if (ModGameRules.isMobGriefEnabled(EntityType.WITHER, (ServerLevel) instance)) {
            return instance.setBlock(pos, blockState, updateFlags);
        }
        return false;
    }


}
