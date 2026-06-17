package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;
import static net.minecraft.commands.Commands.*;

public class BroadcastCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("broadcast")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .executes(BroadcastCommand::noArg)
                .then(
                        argument("value", StringArgumentType.string())
                                .executes(BroadcastCommand::execute)
                )
        );
    }

    private static int noArg(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        source.sendSuccess(() -> TextTools.deserialize("<color:#ff5555>No string to format provided!</color>"), false);
        return 1;
    }

    private static int execute(CommandContext<?> context) {
        String value = StringArgumentType.getString(context, "value");
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        source.getServer().getPlayerList().broadcastSystemMessage(TextTools.deserialize(value), false);
        return 1;
    }
}
