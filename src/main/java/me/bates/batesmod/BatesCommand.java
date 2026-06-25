package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permissions;

import static net.minecraft.commands.Commands.*;


public class BatesCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("bates")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .executes(BatesCommand::execute)
                .then(
                        literal("reload")
                                .executes(BatesCommand::executeReload)
                )
                .then(
                        literal("version")
                                .executes(BatesCommand::executeVersion)
                )
        );
    }

    public static int execute(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        source.sendSuccess(() -> TextTools.builder().input("[%bates%] Welcome to BatesMod!")
                        .placeholder("bates", ConfigManager.get().batesModGradient).build(), false);
        return 1;
    }

    public static int executeReload(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        ConfigManager.load();
        source.sendSuccess(() -> TextTools.builder().input("[%bates%] Config reloaded!")
                .placeholder("bates", ConfigManager.get().batesModGradient).build(), false);
        return 1;
    }

    public static int executeVersion(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        source.sendSuccess(() -> TextTools.builder().input("[%bates%] Current version: %version%")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("version", BatesMod.VERSION).build(), false);
        return 1;
    }
}
