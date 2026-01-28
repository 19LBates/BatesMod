package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakDoorGoal.class)
public abstract class BreakDoorGoalMixin {

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
        DoorInteractGoalAccessor self = (DoorInteractGoalAccessor) this;
        ServerWorld sw = (ServerWorld) self.getMob().getEntityWorld();

        if (!(Boolean) sw.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)
                || !(Boolean) sw.getGameRules().getValue(ModGameRules.ZOMBIE_GRIEFING)) {
            cir.setReturnValue(false);
        }
    }
}
