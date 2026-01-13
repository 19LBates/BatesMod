package me.bates.batesmod.mixin;

import me.bates.batesmod.ConfigManager;
import me.bates.batesmod.Targets;
import me.bates.batesmod.TextTools;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Unique
    private ServerPlayerEntity bates$currentPlayer;

    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        bates$currentPlayer = player;
    }

    @ModifyArg(method = "onPlayerConnect", at = @At(value = "INVOKE", target = Targets.BROADCAST))
    private Text bates$replaceJoinMessage(Text original) {
        return TextTools.deserialize(
                ConfigManager.get().joinMessage,
                new String[]{"player"},
                new String[]{bates$currentPlayer.getName().getString()}
        );
    }
}
