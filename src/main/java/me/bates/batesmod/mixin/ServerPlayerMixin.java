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

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "getTabListDisplayName", at = @At(value = "HEAD"), cancellable = true)
    private void bates$changeTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        PlayerProfileAccessor self = (PlayerProfileAccessor) this;
        String name = self.getGameProfile().name();

        MutableComponent displayName = TextTools.builder()
                .input(ConfigManager.get().displayNames.getOrDefault(name, name))
                .build();

        cir.setReturnValue(displayName);
    }

}
