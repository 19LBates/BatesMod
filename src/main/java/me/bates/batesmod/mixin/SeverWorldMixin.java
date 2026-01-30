package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Pool;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class SeverWorldMixin {

    @Unique
    private Entity entity;
    @Unique
    private World.ExplosionSourceType explosionSourceType;

    @Inject(method = "createExplosion", at = @At("HEAD"))
    private void bates$captureArguments(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, ParticleEffect smallParticle, ParticleEffect largeParticle, Pool<BlockParticleEffect> blockParticles, RegistryEntry<SoundEvent> soundEvent, CallbackInfo ci) {
        this.entity = entity;
        this.explosionSourceType = explosionSourceType;

        if (entity instanceof FireballEntity fireball) {
            this.entity = fireball.getOwner();
        }
    }

    @ModifyArg(method = "createExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/ExplosionImpl;<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;Lnet/minecraft/util/math/Vec3d;FZLnet/minecraft/world/explosion/Explosion$DestructionType;)V"))
    private Explosion.DestructionType bates$changeDestructionType(Explosion.DestructionType original) {
        if (explosionSourceType != World.ExplosionSourceType.MOB) return original;
        if (!ModGameRules.isMobGriefEnabled((MobEntity) entity)) return Explosion.DestructionType.KEEP;
        return original;
    }
}
