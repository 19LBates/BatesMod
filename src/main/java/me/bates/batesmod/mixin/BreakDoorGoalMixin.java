package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.ai.goal.BreakDoorGoal.class)
public abstract class BreakDoorGoalMixin {

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanUse(CallbackInfoReturnable<Boolean> cir) {
        DoorInteractGoalAccessor self = (DoorInteractGoalAccessor) this;
        if (!ModGameRules.isMobGriefEnabled(self.getMob())) {
            cir.setReturnValue(false);
        }
    }
}
