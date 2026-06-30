package io.github.kimovoid.polished.mixin.server.ping;

import io.github.kimovoid.polished.server.PolishedServer;
import net.minecraft.network.packet.LoginPacket;
import net.minecraft.network.packet.PingHostPacket;
import net.minecraft.server.network.handler.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public class ServerLoginNetworkHandlerMixin {

    @Inject(
            method = "acceptLogin",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/entity/mob/player/ServerPlayerEntity;initMenu()V",
                    shift = At.Shift.AFTER
            )
    )
    public void injectLogin(LoginPacket packet, CallbackInfo ci) {
        PolishedServer.SERVER.playerManager.sendPacket(packet.username, new PingHostPacket());
        PolishedServer.INSTANCE.sendPlayerInfo(packet.username, true, 0);
    }
}
