package io.github.kimovoid.polished.server;

import io.github.kimovoid.polished.Polished;
import io.github.kimovoid.polished.networking.PlayerInfoPayload;
import io.github.kimovoid.polished.networking.SoundEventPayload;
import io.github.kimovoid.polished.server.feature.ping.PlayerPing;
import io.github.kimovoid.polished.server.feature.ping.ServerPingPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.ornithemc.osl.entrypoints.api.server.ServerModInitializer;
import net.ornithemc.osl.lifecycle.api.server.MinecraftServerEvents;
import net.ornithemc.osl.networking.api.server.ServerConnectionEvents;
import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;

public class PolishedServer implements ServerModInitializer {

    public static PolishedServer INSTANCE;
    public static MinecraftServer SERVER;
    public PolishedServerProperties properties;

    @Override
    public void initServer() {
        INSTANCE = this;

        /* Events */
        MinecraftServerEvents.LOAD_WORLD.register(server -> {
            SERVER = server;
            this.properties = new PolishedServerProperties(SERVER);
            Packet.register(254, false, true, ServerPingPacket.class);
        });
        ServerConnectionEvents.PLAY_READY.register((server, player) -> this.sendPlayerInfo(player));
    }

    public void sendPlayerInfo(String username, boolean online, int ping) {
        SERVER.playerManager.players.forEach(on -> ServerPlayNetworking.send(on, Polished.PLAYER_INFO_CHANNEL, new PlayerInfoPayload(username, online, ping)));
    }

    public void sendPlayerInfo(ServerPlayerEntity to) {
        for (ServerPlayerEntity on : SERVER.playerManager.players) {
            ServerPlayNetworking.send(to, Polished.PLAYER_INFO_CHANNEL, new PlayerInfoPayload(
                    on.name,
                    true,
                    ((PlayerPing)on).getPing()));
        }
    }

    public void redirectSoundEvent(World world, double x, double y, double z, String sound, float volume, float pitch, PlayerEntity exclude) {
        SoundEventPayload payload = new SoundEventPayload(sound, x, y, z, volume, pitch);
        ServerPlayNetworking.send(
                payload.getRecipients(SERVER, world.dimension.id, exclude),
                Polished.SOUND_EVENT_CHANNEL,
                payload
        );
    }
}
