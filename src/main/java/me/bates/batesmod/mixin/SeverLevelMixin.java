package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class SeverLevelMixin {

    @Unique
    private net.minecraft.world.entity.Entity entity;
    @Unique
    private Level.ExplosionInteraction interactionType;

    @Inject(method = "explode", at = @At("HEAD"))
    private void bates$captureArguments(Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float r, boolean fire, Level.ExplosionInteraction interactionType, ParticleOptions smallExplosionParticles, ParticleOptions largeExplosionParticles, WeightedList<ExplosionParticleInfo> blockParticles, Holder<net.minecraft.sounds.SoundEvent> explosionSound, CallbackInfo ci) {
        this.entity = source;
        this.interactionType = interactionType;
        if (entity instanceof Fireball fireball) {
            this.entity = fireball.getOwner();
        }
    }

    @ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerExplosion;<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;Lnet/minecraft/world/phys/Vec3;FZLnet/minecraft/world/level/Explosion$BlockInteraction;)V"))
    private net.minecraft.world.level.Explosion.BlockInteraction bates$changeDestructionType(net.minecraft.world.level.Explosion.BlockInteraction original) {
        if (interactionType != Level.ExplosionInteraction.MOB) return original;
        if (!ModGameRules.isMobGriefEnabled(entity)) return Explosion.BlockInteraction.KEEP;
        return original;
    }
}
