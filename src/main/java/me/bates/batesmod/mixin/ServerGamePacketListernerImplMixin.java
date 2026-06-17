package me.bates.batesmod.mixin;

import me.bates.batesmod.ConfigManager;
import me.bates.batesmod.TextTools;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListernerImplMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyArg(method = "removePlayerFromWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private Component bates$replaceQuitMessage(Component component) {
        String name = player.getName().getString();
        return TextTools.builder()
                .input(ConfigManager.get().leaveMessage)
                .placeholder("name", name)
                .placeholder("display-name", ConfigManager.get().displayNames.getOrDefault(name, name))
                .build();
    }
}
