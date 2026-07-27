package io.github.kimovoid.polished.networking;

import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.PacketPayload;

import java.io.IOException;
import java.util.List;

public class SoundEventPayload implements PacketPayload {

    private String name;
    private int x;
    private int y = Integer.MAX_VALUE;
    private int z;
    private float volume;
    private int pitch;

    public SoundEventPayload() {
    }

    public SoundEventPayload(String name, double x, double y, double z, float volume, float pitch) {
        this.name = name;
        this.x = (int) (x * 8.0);
        this.y = (int) (y * 8.0);
        this.z = (int) (z * 8.0);
        this.volume = volume;
        this.pitch = (int) (pitch * 63.0F);
        if (this.pitch < 0) {
            this.pitch = 0;
        }

        if (this.pitch > 255) {
            this.pitch = 255;
        }
    }

    public List<ServerPlayerEntity> getRecipients(MinecraftServer server, int dimension, PlayerEntity exclude) {
        double range = this.volume > 1.0F ? 16.0F * this.volume : 16.0;
        return server.playerManager.players.stream()
                .filter(p -> p.dimension == dimension)
                .filter(p -> p.distanceTo(this.getX(), this.getY(), this.getZ()) < range)
                .filter(p -> exclude == null || p != exclude)
                .toList();
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        this.name = buffer.readString(32);
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
        this.volume = buffer.readFloat();
        this.pitch = buffer.readUnsignedByte();
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeString(this.name);
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);
        buffer.writeFloat(this.volume);
        buffer.writeByte(this.pitch);
    }

    public String getSound() {
        return this.name;
    }

    public double getX() {
        return this.x / 8.0F;
    }

    public double getY() {
        return this.y / 8.0F;
    }

    public double getZ() {
        return this.z / 8.0F;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch / 63.0F;
    }
}
