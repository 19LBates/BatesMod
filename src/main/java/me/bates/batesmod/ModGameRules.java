package me.bates.batesmod;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.HashMap;
import java.util.Map;

public class ModGameRules {

    private static final Map<net.minecraft.world.entity.EntityType<?>, GameRule<Boolean>> GAME_RULE_MAP = new HashMap<>();

    public static GameRule<Boolean> registerRule(String name) {
        return GameRuleBuilder.forBoolean(true).category(GameRuleCategory.MOBS).buildAndRegister(Identifier.fromNamespaceAndPath(BatesMod.MOD_ID, name));
    }

    public static boolean isMobGriefEnabled(Entity entity) {
        ServerLevel sw = (ServerLevel) entity.level();
        return isMobGriefEnabled(entity.getType(), sw);
    }

    public static boolean isMobGriefEnabled(net.minecraft.world.entity.EntityType<?> type, ServerLevel sw) {
        if (!GAME_RULE_MAP.containsKey(type)) return true; //Do not interfere with non-implemented mob types
        if (!(Boolean) sw.getGameRules().get(GameRules.MOB_GRIEFING)) return false;
        return sw.getGameRules().get(GAME_RULE_MAP.get(type));
    }

    public static void init() {
        for (net.minecraft.world.entity.EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() == MobCategory.MISC) continue; //Only for mobs
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            GameRule<Boolean> rule = registerRule(id.getPath() + "_griefing");
            GAME_RULE_MAP.put(type, rule);
        }
    }

}
