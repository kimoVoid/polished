package io.github.kimovoid.polished.mixin.common.connection;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Socket;
import java.net.SocketException;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/net/Socket;setTrafficClass(I)V",
                    shift = At.Shift.AFTER
            )
    )
    private void enableTcpNoDelay(Socket socket, String name, PacketHandler listener, CallbackInfo ci) {
        try {
            socket.setTcpNoDelay(true);
        } catch (SocketException ignored) {
        }
    }
}
