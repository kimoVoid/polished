package io.github.kimovoid.polished.server.feature.ping;

import io.github.kimovoid.polished.server.PolishedServer;
import net.minecraft.network.PacketHandler;
import net.minecraft.network.packet.DisconnectPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.handler.ServerLoginNetworkHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ServerPingPacket extends Packet {

    private int ping = 0;

    @Override
    public void read(DataInputStream input) {
        try {
            if (input.available() > 0) { // to avoid timeout
                this.ping = input.readByte();
            }
        } catch (IOException ex) {
            this.ping = 0;
        }
    }

    @Override
    public void write(DataOutputStream output) {
    }

    @Override
    public void handle(PacketHandler handler) {
        String motd = PolishedServer.INSTANCE.properties.serverMotd;
        int players = PolishedServer.SERVER.playerManager.players.size();
        int maxPlayers = PolishedServer.SERVER.properties.getInt("max-players", 20);

        String response;
        if (this.ping == 1) {
            // ping with color code support
            List<Object> fields = Arrays.asList(
                    1,
                    motd,
                    players,
                    maxPlayers
            );

            StringBuilder sb = new StringBuilder("§");
            for (int i = 0; i < fields.size(); i++) {
                if (i > 0) sb.append("\u0000");
                sb.append(fields.get(i).toString().replace("\u0000", ""));
            }
            response = sb.toString();
        } else {
            // basic ping for betatweaks and older mods
            response = motd.replaceAll("§[0-9a-fk-orA-FK-OR]", "") + "§" + players + "§" + maxPlayers;
        }

        ServerLoginNetworkHandler nh = (ServerLoginNetworkHandler)handler;
        if (nh.connection == null) return;
        nh.connection.send(new DisconnectPacket(response));
        nh.connection.close();
        nh.disconnected = true;
    }

    @Override
    public int getSize() {
        return 0;
    }
}
