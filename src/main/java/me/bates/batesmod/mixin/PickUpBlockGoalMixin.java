package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal")
public abstract class PickUpBlockGoalMixin {

    @Final
    @Shadow
    private EndermanEntity enderman;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
        ServerWorld sw = (ServerWorld) enderman.getEntityWorld();
        if (!(Boolean) sw.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)
                || !(Boolean) sw.getGameRules().getValue(ModGameRules.ENDERMAN_GRIEFING)) {
            cir.setReturnValue(false);
        }
    }
}
