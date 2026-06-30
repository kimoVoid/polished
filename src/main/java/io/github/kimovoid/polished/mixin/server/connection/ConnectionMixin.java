package io.github.kimovoid.polished.mixin.server.connection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.server.PolishedServer;
import io.github.kimovoid.polished.server.feature.connection.ExtendedConnectionListener;
import io.github.kimovoid.polished.server.feature.ping.PingHostHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PingHostPacket;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Socket;
import java.util.List;

@Environment(EnvType.SERVER)
@Mixin(Connection.class)
public class ConnectionMixin {

    @Shadow private PacketHandler listener;
    @Shadow private boolean open;
    @Shadow private Socket socket;

    @WrapOperation(method = "read", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private boolean handlePingHost(List<Packet> list, Object obj, Operation<Boolean> original) {
        if (obj instanceof PingHostPacket) {
            ServerPlayNetworkHandler networkHandler = (ServerPlayNetworkHandler) this.listener;
            ((PingHostHandler)networkHandler).handlePingHost((PingHostPacket) obj);
            return false;
        }
        return original.call(list, obj);
    }

    @Inject(method = "close*", at = @At("HEAD"))
    public void removeAddress(CallbackInfo ci) {
        if (this.open) {
            ((ExtendedConnectionListener) PolishedServer.SERVER.connections).close(this.socket);
        }
    }
}
