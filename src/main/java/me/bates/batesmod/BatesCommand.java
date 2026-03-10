package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

public class BatesCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bates")
                .requires(requirePermissionLevel(ADMINS_CHECK))
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
        ServerCommandSource source = (ServerCommandSource) context.getSource();
        source.sendFeedback(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Welcome to BatesMod!"), false);
        return 1;
    }

    public static int executeReload(CommandContext<?> context) {
        ServerCommandSource source = (ServerCommandSource) context.getSource();
        ConfigManager.load();
        source.sendFeedback(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Config reloaded!"), false);
        return 1;
    }

    public static int executeVersion(CommandContext<?> context) {
        ServerCommandSource source = (ServerCommandSource) context.getSource();
        String version = BatesMod.VERSION;
        source.sendFeedback(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Current version: " + version), false);
        return 1;
    }
}
