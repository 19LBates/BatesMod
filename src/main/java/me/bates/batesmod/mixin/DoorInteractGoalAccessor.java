package me.bates.batesmod.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.world.entity.ai.goal.DoorInteractGoal.class)
public interface DoorInteractGoalAccessor {
    @Accessor("mob")
    Mob getMob();
}
