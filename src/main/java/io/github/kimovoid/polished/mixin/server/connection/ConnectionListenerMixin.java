package io.github.kimovoid.polished.mixin.server.connection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.server.feature.connection.ExtendedConnectionListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectionListener;
import net.minecraft.server.network.handler.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

@Mixin(ConnectionListener.class)
public class ConnectionListenerMixin implements ExtendedConnectionListener {

    @Shadow private Thread thread;
    @Shadow public MinecraftServer server;
    @Unique private final HashMap<InetAddress, Long> addresses = new HashMap<>();

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V"))
    public void replaceThread(Thread instance, Operation<Void> original) {
        this.thread = new Thread("Listen thread") {
            public void run() {
                while (server.connections.open) {
                    try {
                        Socket socket = server.connections.socket.accept();
                        if (socket == null) continue;
                        Object object = addresses;
                        synchronized (object) {
                            InetAddress inetAddress = socket.getInetAddress();
                            if (addresses.containsKey(inetAddress) && System.currentTimeMillis() - addresses.get(inetAddress) < 5000L) {
                                addresses.put(inetAddress, System.currentTimeMillis());
                                socket.close();
                                continue;
                            }
                            addresses.put(inetAddress, System.currentTimeMillis());
                        }
                        server.connections.connectionCounter = server.connections.connectionCounter + 1;
                        object = new ServerLoginNetworkHandler(server, socket, "Connection #" + server.connections.connectionCounter);
                        addPendingConnection(server.connections, (ServerLoginNetworkHandler)object);
                    } catch (IOException iOException) {
                        iOException.printStackTrace();
                    }
                }
            }
        };
        this.thread.start();
    }

    @Unique
    private void addPendingConnection(ConnectionListener listener, ServerLoginNetworkHandler connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Got null pendingconnection!");
        }
        listener.pendingConnections.add(connection);
    }

    @Override
    public void close(Socket socket) {
        InetAddress inetAddress = socket.getInetAddress();
        addresses.remove(inetAddress);
    }

    @Override
    public HashMap<InetAddress, Long> getAddresses() {
        return this.addresses;
    }
}
