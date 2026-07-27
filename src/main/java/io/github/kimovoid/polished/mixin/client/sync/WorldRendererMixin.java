package io.github.kimovoid.polished.mixin.client.sync;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.entity.Entities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow private World world;

    @Inject(method = "doEvent", at = @At("TAIL"))
    private void handleSyncEvents(PlayerEntity source, int type, int x, int y, int z, int data, CallbackInfo ci) {
        switch (type) {
            case 6700 -> { // mob spawner
                if (!this.world.isChunkLoaded(x, y, z)) {
                    break;
                }

                Class<? extends Entity> entity = Entities.ID_TO_TYPE.get(data);
                String mobType = entity != null ? Entities.TYPE_TO_KEY.get(entity) : null;

                if (mobType == null) {
                    break;
                }

                BlockEntity blockEntity = this.world.getBlockEntity(x, y, z);
                if (blockEntity instanceof MobSpawnerBlockEntity spawnerBlock) {
                    spawnerBlock.setType(mobType);
                }
            }
        }
    }

    @Inject(
            method = "doEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sound/SoundEngine;play(Ljava/lang/String;FFFFF)V"
            )
    )
    private void removeBlockEntity(PlayerEntity source, int type, int x, int y, int z, int data, CallbackInfo ci) {
        BlockEntity be = this.world.getBlockEntity(x, y, z);
        if (be instanceof MobSpawnerBlockEntity) {
            this.world.removeBlockEntity(x, y, z);
        }
    }
}
