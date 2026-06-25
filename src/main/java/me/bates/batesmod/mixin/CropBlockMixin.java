package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.CropBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
    @Redirect(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    boolean cancelDestroyBlock(ServerLevel instance, BlockPos blockPos, boolean isPrecise, Entity entity) {
        if (ModGameRules.isMobGriefEnabled(EntityType.SHEEP, instance)) {
            return instance.destroyBlock(blockPos, isPrecise, entity);
        }
        return false;
    }
}
