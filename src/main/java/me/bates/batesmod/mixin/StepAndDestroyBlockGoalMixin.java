package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class StepAndDestroyBlockGoalMixin {
//
//    @Final
//    @Shadow
//    private LivingEntity stepAndDestroyMob;
//
//    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
//    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
//        if (!ModGameRules.isMobGriefEnabled(stepAndDestroyMob)) {
//            cir.setReturnValue(false);
//        }
//    }
}
