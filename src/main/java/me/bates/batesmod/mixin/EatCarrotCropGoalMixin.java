package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.passive.RabbitEntity$EatCarrotCropGoal")
public abstract class EatCarrotCropGoalMixin {

    @Final
    @Shadow
    private net.minecraft.entity.passive.RabbitEntity rabbit;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (!ModGameRules.isMobGriefEnabled(rabbit)) {
            cir.setReturnValue(false);
        }
    }
}
