package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

public class BroadcastCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("broadcast")
                .requires(requirePermissionLevel(ADMINS_CHECK))
                .executes(BroadcastCommand::noArg)
                .then(
                        argument("value", StringArgumentType.string())
                                .executes(BroadcastCommand::execute)
                )
        );
    }

    private static int noArg(CommandContext<?> context) {
        ServerCommandSource source = (ServerCommandSource) context.getSource();
        source.sendFeedback(() -> TextTools.deserialize("<color:#ff5555>No string to format provided!</color>"), false);
        return 1;
    }

    private static int execute(CommandContext<?> context) {
        String value = StringArgumentType.getString(context, "value");
        ServerCommandSource source = (ServerCommandSource) context.getSource();
        source.getServer().getPlayerManager().broadcast(TextTools.deserialize(value), false);
        return 1;
    }
}
