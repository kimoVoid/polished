package io.github.kimovoid.polished.mixin.server.connection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.network.packet.WorldChunkPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow public List<ChunkPos> pendingChunks;
    @Shadow public ServerPlayNetworkHandler networkHandler;
    @Shadow public MinecraftServer server;
    @Shadow protected abstract void sendBlockEntityUpdate(BlockEntity blockEntity);

    public ServerPlayerEntityMixin(World world) {
        super(world);
    }

    @WrapOperation(method = "tickPlayer", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean sendChunksFaster(List<ChunkPos> instance, Operation<Boolean> original) {
        List<WorldChunk> chunksToSend = new ArrayList<>();
        List<BlockEntity> blockEntities = new ArrayList<>();

        Iterator<ChunkPos> iterator = this.pendingChunks.iterator();
        while (iterator.hasNext() && chunksToSend.size() < 5) {
            ChunkPos chunkPos = iterator.next();
            iterator.remove();

            if (chunkPos != null && this.world.isChunkLoaded(chunkPos.x << 4, 0, chunkPos.z << 4)) {
                WorldChunk chunk = this.world.getChunkAt(chunkPos.x, chunkPos.z);
                chunksToSend.add(chunk);
                blockEntities.addAll(chunk.blockEntities.values());
            }
        }

        if (!chunksToSend.isEmpty()) {
            ServerWorld serverWorld = this.server.getWorld(this.dimension);

            for (WorldChunk chunk : chunksToSend) {
                this.networkHandler.sendPacket(new WorldChunkPacket(chunk.chunkX * 16, 0, chunk.chunkZ * 16, 16, 128, 16, serverWorld));
            }

            for (BlockEntity be : blockEntities) {
                this.sendBlockEntityUpdate(be);
            }
        }

        return true;
    }
}
