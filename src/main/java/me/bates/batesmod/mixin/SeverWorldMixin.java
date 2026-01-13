package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import me.bates.batesmod.Targets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.particle.BlockParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.collection.Pool;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class SeverWorldMixin {

    @Unique
    private Entity bates$currentExplosionEntity;

    @Inject(method = "createExplosion", at = @At("HEAD"))
    private void bates$captureEntity(@Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, World.ExplosionSourceType explosionSourceType, ParticleEffect smallParticle, ParticleEffect largeParticle, Pool<BlockParticleEffect> blockParticles, RegistryEntry<SoundEvent> soundEvent, CallbackInfo ci) {
        bates$currentExplosionEntity = entity;
    }

    @ModifyArg(method = "createExplosion", at = @At(value = "INVOKE", target = Targets.GET_VALUE))
    private GameRule<?> bates$swapMobGriefingRule(GameRule<?> original) {
        if (original == GameRules.DO_MOB_GRIEFING) {
            if (bates$currentExplosionEntity instanceof CreeperEntity) return ModGameRules.CREEPER_GRIEFING;
            if (bates$currentExplosionEntity instanceof WitherSkullEntity) return ModGameRules.WITHER_GRIEFING;
            if (bates$currentExplosionEntity instanceof WitherEntity) return ModGameRules.WITHER_GRIEFING;
            if (bates$currentExplosionEntity instanceof FireballEntity) return ModGameRules.FIREBALL_GRIEFING;
        }
        return original;
    }
}
