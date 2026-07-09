package me.bates.batesmod.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class MobEntityMixin {
//
//    @Unique
//    Entity self = (Entity) (Object) this;
//
//    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getNonSpectatingEntities(Ljava/lang/Class;Lnet/minecraft/util/math/Box;)Ljava/util/List;"))
//    private <T extends Entity> List<T> bates$cancelPickup(World instance, Class<T> aClass, Box box) {
//        ServerWorld sw = (ServerWorld) instance;
//        if (!ModGameRules.isMobGriefEnabled(self)) {
//            return Collections.emptyList();
//        }
//        return sw.getNonSpectatingEntities(aClass, box);
//    }
}
