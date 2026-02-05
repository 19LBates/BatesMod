package me.bates.batesmod;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;
import net.minecraft.world.rule.GameRules;

import java.util.HashMap;
import java.util.Map;

public class ModGameRules {

    private static final Map<EntityType<?>, GameRule<Boolean>> GAME_RULE_MAP = new HashMap<>();

    public static GameRule<Boolean> registerRule(String name) {
        return GameRuleBuilder.forBoolean(true).category(GameRuleCategory.MOBS).buildAndRegister(Identifier.of(BatesMod.MOD_ID, name));
    }

    public static boolean isMobGriefEnabled(MobEntity entity) {
        ServerWorld sw = (ServerWorld) entity.getEntityWorld();
        return isMobGriefEnabled(entity.getType(), sw);
    }

    public static boolean isMobGriefEnabled(EntityType<?> type, ServerWorld sw) {
        if (!GAME_RULE_MAP.containsKey(type)) return true; //Do not interfere with non-implemented mob types
        if (!(Boolean) sw.getGameRules().getValue(GameRules.DO_MOB_GRIEFING)) return false;
        return sw.getGameRules().getValue(GAME_RULE_MAP.get(type));
    }

    public static void init() {
        for (EntityType<?> type: Registries.ENTITY_TYPE) {
            if (type.getSpawnGroup() == SpawnGroup.MISC) continue; //Only for mobs
            Identifier id = Registries.ENTITY_TYPE.getId(type);
            GameRule<Boolean> rule = registerRule(id.getPath() + "_griefing");
            GAME_RULE_MAP.put(type, rule);
        }
    }

}
