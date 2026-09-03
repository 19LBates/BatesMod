package me.bates.batesmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.bates.batesmod.ConfigManager;
import me.bates.batesmod.TextTools;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Unique
    private ServerPlayer player;

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    public void onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        this.player = player;
    }

    @ModifyArg(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private Component bates$replaceJoinMessage(Component component) {
        String name = player.getName().getString();
        UUID uuid = player.getUUID();
        return TextTools.builder()
                .input(ConfigManager.get().joinMessage)
                .placeholder("name", name)
                .placeholder("display-name", ConfigManager.get().displayNames.getOrDefault(uuid, name))
                .build();
    }

    @ModifyReturnValue(method = "canPlayerLogin", at = @At(value = "RETURN"))
    public Component newMessage(Component original) {
        if (original == null) return null;
        if (original.equals(Component.translatable("multiplayer.disconnect.not_whitelisted")))
            return TextTools.builder()
                    .input(ConfigManager.get().notWhitelistedMessage)
                    .build();
        return original;
    }
}
