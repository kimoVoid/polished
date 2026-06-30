package io.github.kimovoid.polished.mixin.client.connection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PingHostPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow public abstract void send(Packet packet);

    @WrapOperation(
            method = "read",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            )
    )
    private boolean instantReadPackets(List<Packet> queue, Object p, Operation<Boolean> original) {
        if (p instanceof PingHostPacket) {
            this.send(new PingHostPacket());
            return true;
        }
        return original.call(queue, p);
    }

    @WrapOperation(method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/Packet;handle(Lnet/minecraft/network/PacketHandler;)V"
            )
    )
    private void handleKeepAlive(Packet packet, PacketHandler packetHandler, Operation<Void> original) {
        if (packet instanceof PingHostPacket) {
            this.send(new PingHostPacket());
            return;
        }
        original.call(packet, packetHandler);
    }
}
