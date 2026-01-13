package me.bates.batesmod;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class QuitMessage {

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            Text quitText = TextTools.deserialize(
                    "<gradient:41ff6d:72baff>%player% hopped off</gradient>",
                    new String[]{"player"},
                    new String[]{player.getName().getString()}
            );

            server.getPlayerManager().broadcast(quitText, false);
        });
    }
}
