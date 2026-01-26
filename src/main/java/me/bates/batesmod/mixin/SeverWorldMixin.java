package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerWorld.class)
public abstract class SeverWorldMixin {

    @Unique
    private final Map<EntityType<?>, GameRule<?>> gameRuleMap = Map.of(
            EntityType.CREEPER, ModGameRules.CREEPER_GRIEFING,
            EntityType.GHAST, ModGameRules.GHAST_GRIEFING,
            EntityType.WITHER, ModGameRules.WITHER_GRIEFING,
            EntityType.WITHER_SKULL, ModGameRules.WITHER_GRIEFING
    );

    @Shadow
    public abstract GameRules getGameRules();

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
        if (!(Boolean) this.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)) return Explosion.DestructionType.KEEP;

        for (EntityType<?> type : gameRuleMap.keySet()) {
            if (entity.getType() == type && !(Boolean) this.getGameRules().getValue(gameRuleMap.get(entity.getType()))) {
                return Explosion.DestructionType.KEEP;
            }
        }

        return original;
    }
}
