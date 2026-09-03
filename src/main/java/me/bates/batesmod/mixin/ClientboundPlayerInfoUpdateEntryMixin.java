package me.bates.batesmod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/network/protocol/game/ClientboundPlayerInfoUpdatePacket$Entry")
public abstract class ClientboundPlayerInfoUpdateEntryMixin {
    @Redirect(
            method = "<init>(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getGameProfile()Lcom/mojang/authlib/GameProfile;")
    )
    private static GameProfile bates$spoofProfileName(ServerPlayer player) {
        GameProfile real = player.getGameProfile();
        String override = "";
        return new GameProfile(real.id(), override, real.properties());
    }
}
