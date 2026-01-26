package me.bates.batesmod.mixin;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Unique
    MobEntity self = (MobEntity)(Object) this;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void bates$(CallbackInfo ci) {
        if (self instanceof ZombieEntity) {
            //idk
        }
    }
}
