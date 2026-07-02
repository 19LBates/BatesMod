package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.permissions.Permissions;

import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DisplayNameCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("display-name")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
                .then(
                        literal("remove")
                                .then(
                                        argument("username", StringArgumentType.string())
                                                .suggests((context, builder) ->
                                                        SharedSuggestionProvider.suggest(context.getSource().getServer().getPlayerNames(), builder))
                                                .executes((context) ->
                                                        removeDisplayName(context, StringArgumentType.getString(context, "username")))
                                )
                )
                .then(
                        literal("add")
                                .then(
                                        argument("username", StringArgumentType.string())
                                                .suggests((context, builder) ->
                                                        SharedSuggestionProvider.suggest(context.getSource().getServer().getPlayerNames(), builder))
                                                .then(
                                                        argument("display-name", StringArgumentType.string())
                                                                .executes(DisplayNameCommand::addDisplayName)
                                                )
                                )
                )
        );
    }

    private static int addDisplayName(CommandContext<CommandSourceStack> context) {
        String username = StringArgumentType.getString(context, "username");
        String displayName = StringArgumentType.getString(context, "display-name");

        if (Objects.equals(username, displayName)) {
            return removeDisplayName(context, username);
        }

        ConfigManager.get().displayNames.put(username, displayName);
        ConfigManager.save();

        context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] User %username% is now called %display-name%")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("username", username)
                .placeholder("display-name", displayName)
                .build());

        return 1;
    }

    private static int removeDisplayName(CommandContext<CommandSourceStack> context, String username) {
        ConfigManager.get().displayNames.remove(username);
        ConfigManager.save();

        context.getSource().sendSystemMessage(TextTools.builder().input("[%bates%] Removed display name of %username%")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("username", username)
                .build());

        return 1;
    }

}
