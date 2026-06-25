package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.level.block.TurtleEggBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TurtleEggBlock.class)
public abstract class TurtleEggBlockMixin {
    @Inject(method = "canDestroyEgg", at = @At("HEAD"), cancellable = true)
    private void canDestroyEgg(ServerLevel level, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Turtle || entity instanceof Bat) {
            cir.setReturnValue(false);
        } else {
            if (entity instanceof LivingEntity) cir.setReturnValue(false);
            cir.setReturnValue(ModGameRules.isMobGriefEnabled(entity));
        }
    }
}
