package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.entity.mob.EndermanEntity$PickUpBlockGoal")
public abstract class PickUpBlockGoalMixin extends Goal {


    @Final
    @Shadow
    private EndermanEntity enderman;

    @Unique
    private final ServerWorld sw = getServerWorld(enderman);

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (!(Boolean) sw.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)
                || !(Boolean) sw.getGameRules().getValue(ModGameRules.ENDERMAN_GRIEFING)) {
            cir.setReturnValue(false);
        }
    }
}
