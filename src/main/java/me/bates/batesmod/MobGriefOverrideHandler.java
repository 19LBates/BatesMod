package me.bates.batesmod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.gamerules.GameRules;

public class MobGriefOverrideHandler {

    public static boolean isMobGriefEnabled(Entity entity) {
        ServerLevel level = (ServerLevel) entity.level();
        return isMobGriefEnabled(entity.getType(), level);
    }

    public static boolean isMobGriefEnabled(EntityType<?> type, ServerLevel level) {
        var mobGriefMap = ConfigManager.get().mobGriefMap;
        String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        if (mobGriefMap.containsKey(typeId)) {
            if (mobGriefMap.get(typeId).equals(MobGriefOverride.ALLOW)) return true;
            if (mobGriefMap.get(typeId).equals(MobGriefOverride.DENY)) return false;
        }
        return level.getGameRules().get(GameRules.MOB_GRIEFING);
    }


    //UNUSED: if mob does not appear in HashMap, then treat as DEFAULT
    /*
    public static void init() {
        var mobGriefMap = ConfigManager.get().mobGriefMap;
        boolean changed = false;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() == MobCategory.MISC) continue; //Only for mobs
            String typeId = type.toString();
            if (!mobGriefMap.containsKey(typeId)) {
                mobGriefMap.put(typeId, MobGriefOverride.DEFAULT);
                changed = true;
            }
        }
        if (changed) {ConfigManager.save();}
    }
    */

}
