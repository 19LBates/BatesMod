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
        source.sendSuccess(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Welcome to BatesMod!"), false);
        return 1;
    }

    public static int executeReload(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        ConfigManager.load();
        source.sendSuccess(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Config reloaded!"), false);
        return 1;
    }

    public static int executeVersion(CommandContext<?> context) {
        CommandSourceStack source = (CommandSourceStack) context.getSource();
        String version = BatesMod.VERSION;
        source.sendSuccess(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Current version: " + version), false);
        return 1;
    }
}
