package me.bates.batesmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.PlayerList;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class DisplayNameCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("display_name")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_ADMIN))

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

                .then(
                        literal("remove")
                                .then(
                                        argument("username", StringArgumentType.string())
                                                .suggests((context, builder) ->
                                                        SharedSuggestionProvider.suggest(context.getSource().getServer().getPlayerNames(), builder))
                                                .executes(DisplayNameCommand::removeDisplayName)
                                )
                )

                .then(
                        literal("search")
                                .then(
                                        argument("display-name", StringArgumentType.string())
                                                .executes(DisplayNameCommand::searchDisplayName)
                                )
                )
        );
    }

    private static int addDisplayName(CommandContext<CommandSourceStack> context) {
        String username = StringArgumentType.getString(context, "username");
        String displayName = StringArgumentType.getString(context, "display-name");
        PlayerList playerList = null;
        ServerPlayer player = null;
        UUID uuid;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            uuid = Objects.requireNonNull(Minecraft.getInstance().player).getUUID();
        } else {
            playerList = context.getSource().getServer().getPlayerList();
            player = Objects.requireNonNull(playerList.getPlayer(username));
            uuid = player.getUUID();
        }

        if (Objects.equals(username, displayName)) {
            return removeDisplayName(context);
        }

        ConfigManager.get().displayNames.put(uuid, displayName);
        ConfigManager.save();

        if (playerList != null)
            playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

        context.getSource().sendSystemMessage(TextTools.builder()
                .input("%bates% User %username% is now called %display-name%")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("username", username)
                .placeholder("display-name", displayName)
                .build()
        );

        return 1;
    }

    private static int removeDisplayName(CommandContext<CommandSourceStack> context) {
        String username = StringArgumentType.getString(context, "username");
        PlayerList playerList = null;
        ServerPlayer player = null;
        UUID uuid;

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            uuid = Objects.requireNonNull(Minecraft.getInstance().player).getUUID();
        } else {
            playerList = context.getSource().getServer().getPlayerList();
            player = Objects.requireNonNull(playerList.getPlayer(username));
            uuid = player.getUUID();
        }

        ConfigManager.get().displayNames.remove(uuid);
        ConfigManager.save();

        if (playerList != null)
            playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));

        context.getSource().sendSystemMessage(TextTools.builder()
                .input("%bates% Removed display name of %username%")
                .placeholder("bates", ConfigManager.get().batesModGradient)
                .placeholder("username", username)
                .build()
        );

        return 1;
    }

    private static int searchDisplayName(CommandContext<CommandSourceStack> context) {
        String displayName = StringArgumentType.getString(context, "display-name");
        ModConfig config = ConfigManager.get();
        Map<UUID, String> displayNames = config.displayNames;
        UUID uuid = displayNames.keySet()
                .stream()
                .filter(s -> displayNames.get(s).equals(displayName))
                .findFirst()
                .orElse(null);

        if (uuid == null) {
            context.getSource().sendSystemMessage(TextTools.builder()
                    .input("%bates% <c>No user found with display name of %display-name%")
                    .placeholder("bates", ConfigManager.get().batesModGradient)
                    .placeholder("display-name", displayName)
                    .build()
            );
            return 0;
        }

        //Try to find username
        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(uuid);
        String username = "";
        if (player != null) {
            username = player.getPlainTextName() + " ";
        }

        context.getSource().sendSystemMessage(TextTools.builder()
                .input("%bates% Display name of %display-name% belongs to user %username%with UUID <copy><u>%uuid%</u></copy>.")
                .placeholder("bates", config.batesModGradient)
                .placeholder("display-name", displayName)
                .placeholder("username", username)
                .placeholder("uuid", uuid.toString())
                .build()
        );

        return 1;
    }
}
