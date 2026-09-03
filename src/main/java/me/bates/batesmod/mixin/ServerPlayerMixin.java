package me.bates.batesmod.mixin;

import me.bates.batesmod.ConfigManager;
import me.bates.batesmod.TextTools;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import java.util.UUID;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Inject(method = "getTabListDisplayName", at = @At(value = "HEAD"), cancellable = true)
    private void bates$changeTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        PlayerProfileAccessor self = (PlayerProfileAccessor) this;
        String name = self.getGameProfile().name();
        UUID uuid = self.getGameProfile().id();

        MutableComponent displayName = TextTools.builder()
                .input(ConfigManager.get().displayNames.getOrDefault(uuid, name))
                .build();

        cir.setReturnValue(displayName);
    }

}
