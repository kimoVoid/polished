package io.github.kimovoid.polished.server;

import net.minecraft.server.MinecraftServer;

public class PolishedServerProperties {

    public String serverMotd;
    public boolean deathCoordinates;
    public int spawnProtection;
    public int difficulty;
    public int playerSleepingPercentage;

    public PolishedServerProperties(MinecraftServer server) {
        this.serverMotd = server.properties.getString("motd", "A Minecraft Server");
        this.deathCoordinates = server.properties.getBoolean("death-coordinates", false);
        this.spawnProtection = server.properties.getInt("spawn-protection", 16);
        this.difficulty = server.properties.getInt("difficulty", 1);
        this.playerSleepingPercentage = server.properties.getInt("player-sleeping-percentage", 100);
        server.properties.save();
    }
}
