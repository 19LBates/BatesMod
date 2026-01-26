package me.bates.batesmod;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleCategory;

public class ModGameRules {

    public static final GameRule<Boolean> CREEPER_GRIEFING = registerRule("creeper_griefing");
    public static final GameRule<Boolean> ENDERMAN_GRIEFING = registerRule("enderman_griefing");
    public static final GameRule<Boolean> GHAST_GRIEFING = registerRule("ghast_griefing");
    public static final GameRule<Boolean> SHEEP_GRIEFING = registerRule("sheep_griefing");
    public static final GameRule<Boolean> WITHER_GRIEFING = registerRule("wither_griefing");
    public static final GameRule<Boolean> ZOMBIE_GRIEFING = registerRule("zombie_griefing");
    public static final GameRule<Boolean> PICKUP_ITEMS = registerRule("mobs_pickup_items");



    public static GameRule<Boolean> registerRule(String name) {
        return GameRuleBuilder.forBoolean(true).category(GameRuleCategory.MOBS).buildAndRegister(Identifier.of(BatesMod.MOD_ID, name));
    }

    public static void init() {
    }

}
