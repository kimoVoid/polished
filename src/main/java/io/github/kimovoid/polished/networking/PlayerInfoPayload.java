package io.github.kimovoid.polished.networking;

import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.PacketPayload;

public class PlayerInfoPayload implements PacketPayload {

    public String username;
    public boolean isOnline;
    public int ping;

    public PlayerInfoPayload() {
    }

    public PlayerInfoPayload(String name, boolean isConnected, int ping) {
        this.username = name;
        this.isOnline = isConnected;
        this.ping = ping;
    }

    @Override
    public void read(PacketBuffer input) {
        this.username = input.readString(16);
        this.isOnline = input.readByte() != 0;
        this.ping = input.readShort();
    }

    @Override
    public void write(PacketBuffer output) {
        output.writeString(this.username);
        output.writeByte(this.isOnline ? 1 : 0);
        output.writeShort(this.ping);
    }
}
