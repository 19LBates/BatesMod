package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

public class SkibCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("skib")
                .executes(context -> output(context.getSource()))
        );
    }

    private static int output(ServerCommandSource source) {
        source.sendFeedback(() -> TextTools.deserialize("<gradient:ffff00:ff0000>Skibidi Rizz from Ohio!</gradient>"), false);
        return 1;
    }
}
