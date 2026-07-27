package io.github.kimovoid.polished.mixin.server.sync.spawner;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.Entities;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.WorldEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.SERVER)
@Mixin(MobSpawnerBlockEntity.class)
public abstract class MobSpawnerBlockEntityMixin extends BlockEntity {

    @Shadow private String type;

    @Override
    public Packet createUpdatePacket() {
        return new WorldEventPacket(6700, this.x, this.y, this.z, this.getIdOrPig(this.type));
    }

    @Unique
    private int getIdOrPig(String type) {
        int id = 90;
        Class<? extends Entity> entity = Entities.KEY_TO_TYPE.get(type);
        if (entity != null) {
            id = Entities.TYPE_TO_ID.get(entity);
        }
        return id;
    }
}
