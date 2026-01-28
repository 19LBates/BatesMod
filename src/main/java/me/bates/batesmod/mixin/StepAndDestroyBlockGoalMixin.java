package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRule;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(StepAndDestroyBlockGoal.class)
public abstract class StepAndDestroyBlockGoalMixin {

    @Unique
    private static final Map<EntityType<?>, GameRule<?>> gameRuleMap = Map.of(
            EntityType.DROWNED, ModGameRules.ZOMBIE_GRIEFING,
            EntityType.HUSK, ModGameRules.ZOMBIE_GRIEFING,
            EntityType.ZOMBIE, ModGameRules.ZOMBIE_GRIEFING,
            EntityType.ZOMBIE_VILLAGER, ModGameRules.ZOMBIE_GRIEFING
    );

    @Final
    @Shadow
    private MobEntity stepAndDestroyMob;

    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void bates$changeCanStart(CallbackInfoReturnable<Boolean> cir) {
        ServerWorld sw = (ServerWorld) stepAndDestroyMob.getEntityWorld();
        for (EntityType<?> type : gameRuleMap.keySet()) {
            if (stepAndDestroyMob.getType() == type && !(Boolean) sw.getGameRules().getValue(gameRuleMap.get(type))) {
                cir.setReturnValue(false);
            }
        }
    }
}
