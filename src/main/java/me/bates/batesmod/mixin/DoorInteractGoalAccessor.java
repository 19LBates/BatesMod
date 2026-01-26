package me.bates.batesmod.mixin;

import net.minecraft.entity.ai.goal.DoorInteractGoal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoorInteractGoal.class)
public interface DoorInteractGoalAccessor {
    @Accessor("mob")
    MobEntity getMob();
}
