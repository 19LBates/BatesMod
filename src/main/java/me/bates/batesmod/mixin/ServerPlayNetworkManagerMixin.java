package me.bates.batesmod.mixin;

import me.bates.batesmod.ConfigManager;
import me.bates.batesmod.Targets;
import me.bates.batesmod.TextTools;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkManagerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @ModifyArg(method = "cleanUp", at = @At(value = "INVOKE", target = Targets.BROADCAST))
    private Text yo(Text text) {
        return TextTools.deserialize(
                ConfigManager.get().leaveMessage,
                new String[]{"player"},
                new String[]{player.getName().getString()}
        );
    }
}
