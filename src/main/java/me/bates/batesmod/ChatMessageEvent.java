package me.bates.batesmod;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ChatMessageEvent {
    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
                    String name = sender.getName().getString();
                    String content = message.getContent().getString();
                    for (ServerPlayerEntity player : sender.getEntityWorld().getPlayers()) {
                        player.sendMessage(TextTools.builder().input(ConfigManager.get().chatMessageFormat)
                                .placeholder("name", name)
                                .placeholder("message", content)
                                .placeholder("display-name", ConfigManager.get().displayNames.getOrDefault(name, name))
                                .build()
                        );
                    }
                    return false;
                }
        );
    }
}
