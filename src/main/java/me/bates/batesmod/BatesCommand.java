package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

public class BatesCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bates")
                .requires(requirePermissionLevel(ADMINS_CHECK))
                .executes(context -> execute(context.getSource()))
                .then(
                        literal("reload")
                                .executes(context -> executeReload(context.getSource()))
                )
                .then(
                        literal("version")
                                .executes(context -> executeVersion(context.getSource()))
                )
        );
    }

    public static int execute(ServerCommandSource source) {
        source.sendFeedback(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Welcome to BatesMod!"), false);
        return 1;
    }

    public static int executeReload(ServerCommandSource source) {
        ConfigManager.load();
        source.sendFeedback(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Config reloaded!"), false);
        return 1;
    }

    public static int executeVersion(ServerCommandSource source) {
        String version = BatesMod.VERSION;
        source.sendFeedback(() -> TextTools.deserialize("[<gradient:#7b41f0:#57bff2>BatesMod</gradient>] Current version: " + version), false);
        return 1;
    }
}
