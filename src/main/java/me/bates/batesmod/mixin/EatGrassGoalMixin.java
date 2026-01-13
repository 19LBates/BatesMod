package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import me.bates.batesmod.Targets;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(EatGrassGoal.class)
public abstract class EatGrassGoalMixin {

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = Targets.GET_VALUE))
    private GameRule<?> bates$swapMobGriefingRule(GameRule<?> original) {
        if (original == GameRules.DO_MOB_GRIEFING) return ModGameRules.SHEEP_GRIEFING;
        return original;
    }

}
