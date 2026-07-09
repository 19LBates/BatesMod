package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntityType;

import java.util.EnumSet;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MobGriefCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(literal("mob-grief")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(argument("mob", ResourceArgument.resource(context, Registries.ENTITY_TYPE))
                        .suggests((_, builder) ->
                                SharedSuggestionProvider.suggestResource(
                                        BuiltInRegistries.ENTITY_TYPE.stream(),
                                        builder,
                                        EntityType::getKey,
                                        EntityType::getDescription
                                )
                        )
                        .executes(MobGriefCommand::getValue)
                        .then(argument("value", StringArgumentType.string())
                                .suggests((_, builder) ->
                                        SharedSuggestionProvider.suggest(EnumSet.allOf(MobGriefOverride.class)
                                                .stream().map(Enum::toString), builder))
                                .executes(MobGriefCommand::setValue)
                        )
                )
        );

    }

    private static int getValue(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String mobName = ResourceArgument.getResource(context, "mob", Registries.ENTITY_TYPE).getRegisteredName();
        String message = "";
        MobGriefOverride value = ConfigManager.get().mobGriefMap.get(mobName);
        if (value == null) {
            message = "%mob% has no mob griefing override.";
        } else if (value.equals(MobGriefOverride.ALLOW)) {
            message = "%mob% has a mob grief override value of <a>ALLOW</a>.";
        } else if (value.equals(MobGriefOverride.DENY)) {
            message = "%mob% has a mob grief override value of <c>DENY</c>.";
        } else if (value.equals(MobGriefOverride.DEFAULT)) {
            message = "%mob% has no mob grief override.";
        }
        context.getSource().sendSystemMessage(TextTools.builder().input(message).placeholder("mob", mobName).build());
        return 1;
    }

    private static int setValue(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String mobName = ResourceArgument.getResource(context, "mob", Registries.ENTITY_TYPE).getRegisteredName();
        MobGriefOverride value = Enum.valueOf(MobGriefOverride.class, StringArgumentType.getString(context, "value"));
        String message = "";

        if (value.equals(MobGriefOverride.ALLOW)) {
            ConfigManager.get().mobGriefMap.put(mobName, value);
            message = "%mob% now has override value of <a>ALLOW</a>.";
        } else if (value.equals(MobGriefOverride.DENY)) {
            ConfigManager.get().mobGriefMap.put(mobName, value);
            message = "%mob% now has override value of <c>DENY</c>.";
        } else if (value.equals(MobGriefOverride.DEFAULT)) {
            ConfigManager.get().mobGriefMap.remove(mobName);
            message = "%mob% now has no mob grief override.";
        }
        ConfigManager.save();
        context.getSource().sendSystemMessage(TextTools.builder().input(message).placeholder("mob", mobName).build());
        return 1;
    }
}
