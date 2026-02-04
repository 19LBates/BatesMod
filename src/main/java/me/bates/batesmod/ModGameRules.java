package me.bates.batesmod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.rule.GameRules;

public class ModGameRules {

    public static boolean isMobGriefEnabled(MobEntity entity) {
        ServerWorld sw = (ServerWorld) entity.getEntityWorld();
        return isMobGriefEnabled(entity.getType(), sw);
    }

    public static boolean isMobGriefEnabled(EntityType<?> type, ServerWorld sw) {
        if (!(Boolean) sw.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)) return false;
        return !ConfigManager.get().disabledGriefMobs.contains(Registries.ENTITY_TYPE.getId(type).toString());
    }

    public static void init() {
    }
}
