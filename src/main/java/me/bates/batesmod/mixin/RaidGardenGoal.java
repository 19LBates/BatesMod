package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.animal.rabbit.Rabbit$RaidGardenGoal")
public abstract class RaidGardenGoal {

    @Final
    @Shadow
    private Rabbit rabbit;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (!ModGameRules.isMobGriefEnabled(rabbit)) {
            cir.setReturnValue(false);
        }
    }
}
