package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakDoorGoal.class)
public abstract class BreakDoorGoalMixin extends DoorInteractGoal {

    @Unique
    private final DoorInteractGoalAccessor self = (DoorInteractGoalAccessor) this;
    @Unique
    private final ServerWorld sw = getServerWorld(self.getMob());

    public BreakDoorGoalMixin(MobEntity mob) {
        super(mob);
    }

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (!(Boolean) sw.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)
                || !(Boolean) sw.getGameRules().getValue(ModGameRules.ZOMBIE_GRIEFING)) {
            cir.setReturnValue(false);
        }
    }
}
