package me.bates.batesmod;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.permissions.Permissions;

public class ChatMessageEvent {
    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, _) -> {
                    String name = sender.getName().getString();
                    String content = message.signedContent();
                    try {
                        sender.level().getServer().getPlayerList().broadcastSystemMessage(
                                TextTools.builder().input(ConfigManager.get().chatMessageFormat)
                                        .placeholder("name", name)
                                        .placeholder("message", content, !sender.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                                        .placeholder("display-name", ConfigManager.get().displayNames.getOrDefault(name, name))
                                        .build(), false
                        );
                        return false;
                    } catch (Exception e) {
                        sender.sendSystemMessage(TextTools.builder().input("[%bates%] <c>Error parsing message:</c> %error%")
                                .placeholder("bates", ConfigManager.get().batesModGradient)
                                .placeholder("error", e.getMessage())
                                .build()
                        );
                    }
                    return false;
                }
        );

    }
}
