package io.github.kimovoid.polished.mixin.common.connection;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow private Thread reader;
    @Shadow private Thread writer;
    @Shadow public boolean closed;
    @Unique private final Object writeLock = new Object();

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/net/Socket;setTrafficClass(I)V", shift = At.Shift.AFTER))
    private void enableTcpNoDelay(Socket socket, String name, PacketHandler listener, CallbackInfo ci) {
        try {
            socket.setTcpNoDelay(true);
        } catch (SocketException ignored) {
        }
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V", ordinal = 0))
    private void replaceThreads(Socket socket, String name, PacketHandler listener, CallbackInfo ci) {
        Connection self = (Connection) (Object) this;
        this.reader = new Thread(name + " read thread") {
            public void run() {
                synchronized (Connection.LOCK) {
                    Connection.READ_THREAD_COUNTER++;
                }

                try {
                    while (self.open && !self.closed) {
                        while (self.read()) {
                        }

                        try {
                            sleep(5L);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } finally {
                    synchronized (Connection.LOCK) {
                        Connection.READ_THREAD_COUNTER--;
                    }
                }
            }
        };

        this.writer = new Thread(name + " write thread") {
            public void run() {
                synchronized (Connection.LOCK) {
                    Connection.WRITE_THREAD_COUNTER++;
                }

                try {
                    while (self.open) {
                        boolean wroteSomething = false;
                        while (self.write()) {
                            wroteSomething = true;
                        }

                        if (wroteSomething) {
                            try {
                                if (self.output != null) {
                                    self.output.flush();
                                }
                            } catch (IOException ex) {
                                if (!self.disconnected) {
                                    self.handleException(ex);
                                }
                            }
                        }

                        synchronized (writeLock) {
                            if (self.sendQueue.isEmpty() && self.delayedSendQueue.isEmpty()) {
                                try {
                                    writeLock.wait(50L);
                                } catch (InterruptedException ignored) {
                                }
                            }
                        }
                    }
                } finally {
                    synchronized (Connection.LOCK) {
                        Connection.WRITE_THREAD_COUNTER--;
                    }
                }
            }
        };
    }

    @Inject(method = "send", at = @At("TAIL"))
    private void wakeWriter(Packet packet, CallbackInfo ci) {
        if (!this.closed) {
            synchronized (this.writeLock) {
                this.writeLock.notifyAll();
            }
        }
    }
}
