package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;
import static net.minecraft.server.command.CommandManager.literal;

public class GriefCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("grief")
                .requires(requirePermissionLevel(ADMINS_CHECK))
                .executes(context -> execute(context.getSource()))
                .then(argument("entity", StringArgumentType.string())
                        .suggests((context, builder) ->
                                CommandSource.listSuggestions(Registries.ENTITY_TYPE.getIds()))
                )
        );
    }

    public static int execute(ServerCommandSource source) {
        source.sendFeedback(() -> TextTools.deserialize("<color:ff5555>Usage: /grief <mob> [true|false]</color>"), false);
        return 1;
    }
}

