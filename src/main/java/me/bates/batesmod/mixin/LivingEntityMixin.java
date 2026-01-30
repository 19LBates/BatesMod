package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(method = "onKilledBy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean bates$stopWitherRose(World instance, BlockPos pos, BlockState state, int flags) {
        ServerWorld sw = (ServerWorld) instance;
        if (ModGameRules.isMobGriefEnabled(EntityType.WITHER, sw)) {
            return instance.setBlockState(pos, state, flags);
        }
        return false;
    }


}
