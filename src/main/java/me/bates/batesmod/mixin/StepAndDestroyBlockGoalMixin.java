package me.bates.batesmod.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

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
