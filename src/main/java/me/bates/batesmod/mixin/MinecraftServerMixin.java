package me.bates.batesmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.bates.batesmod.ConfigManager;
import me.bates.batesmod.TextTools;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @ModifyExpressionValue(method = "buildServerStatus", at = @At(value = "NEW", target = "(Lnet/minecraft/network/chat/Component;Ljava/util/Optional;Ljava/util/Optional;Ljava/util/Optional;Z)Lnet/minecraft/network/protocol/status/ServerStatus;"))
    private ServerStatus newStatus(ServerStatus original) {
        MutableComponent newMotd = TextTools.builder().input(ConfigManager.get().motd).build();
        return new ServerStatus(newMotd, original.players(), original.version(), original.favicon(), original.enforcesSecureChat());
    }
}
