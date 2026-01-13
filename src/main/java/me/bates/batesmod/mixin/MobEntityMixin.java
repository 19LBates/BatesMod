package me.bates.batesmod.mixin;

import me.bates.batesmod.ModGameRules;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MobEntityMixin {

    @Inject(
            method = "canModifyAt(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void bates$disableMobGriefing(ServerWorld world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object) this;

        Identifier id = Registries.ENTITY_TYPE.getId(self.getType());

        if (ModGameRules.isMobGriefDisabled(id.toString())) {
            cir.setReturnValue(false);
        }
    }
}
