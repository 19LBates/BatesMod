package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import static net.minecraft.commands.Commands.*;

public class SkibCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("skib")
                .executes(SkibCommand::output)
        );
    }

    private static int output(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        source.sendSuccess(() -> TextTools.deserialize("<gradient:ffff00:ff0000>Skibidi Rizz from Ohio!</gradient>"), false);
        return 1;
    }
}
