package io.github.kimovoid.polished.server.feature.ping;

import net.minecraft.network.packet.PingHostPacket;

public interface PingHostHandler {
    void handlePingHost(PingHostPacket packet);
}
