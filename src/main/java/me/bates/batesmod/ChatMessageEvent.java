package me.bates.batesmod;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class ChatMessageEvent {
    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
                    String name = sender.getName().getString();
                    String content = message.getContent().getString();
                    for (ServerPlayerEntity player : sender.getEntityWorld().getPlayers()) {
                        player.sendMessage(TextTools.deserialize((ConfigManager.get().chatMessageFormat),
                                new String[]{"name", "message"},
                                new String[]{name, content}));
                    }
                    return false;
                }
        );
    }
}
