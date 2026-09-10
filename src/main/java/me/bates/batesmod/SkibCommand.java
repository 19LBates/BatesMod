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

    private static int output(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> TextTools.builder().input("<gradient:ffff00:ff0000>Skibidi Rizz from Ohio!</gradient>").build(), false);
        return 1;
    }
}
