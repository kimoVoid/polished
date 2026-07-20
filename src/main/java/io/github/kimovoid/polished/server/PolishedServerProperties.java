package io.github.kimovoid.polished.server;

import net.minecraft.server.MinecraftServer;

public class PolishedServerProperties {

    public String serverMotd;
    public boolean onePlayerSleep;
    public boolean deathCoordinates;
    public int spawnProtection;
    public int difficulty;

    public PolishedServerProperties(MinecraftServer server) {
        this.serverMotd = server.properties.getString("motd", "A Minecraft Server");
        this.onePlayerSleep = server.properties.getBoolean("one-player-sleep", false);
        this.deathCoordinates = server.properties.getBoolean("death-coordinates", false);
        this.spawnProtection = server.properties.getInt("spawn-protection", 16);
        this.difficulty = server.properties.getInt("difficulty", 1);
        server.properties.save();
    }
}
