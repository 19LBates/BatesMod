package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.minecraft.block.FarmlandBlock.setToDirt;


@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @Redirect(method = "onLandedUpon", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FarmlandBlock;setToDirt(Lnet/minecraft/entity/Entity;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void bates$cancelSetToDirt(Entity entity, BlockState state, World world, BlockPos pos) {
        if (ModGameRules.isMobGriefEnabled((MobEntity) entity)) {
            setToDirt(entity, state, world, pos);
        }
    }

}
