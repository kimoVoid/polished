package io.github.kimovoid.polished.mixin.client.sync;

import io.github.kimovoid.polished.client.feature.sync.FishingBobberSync;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FishingBobberEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends Entity implements FishingBobberSync {

    @Shadow public PlayerEntity thrower;

    public FishingBobberEntityMixin(World world) {
        super(world);
    }

    @Override
    public void setThrower(PlayerEntity entity) {
        this.thrower = entity;
        this.thrower.fishingBobber = (FishingBobberEntity) (Object) this;
    }

    @Override
    public void remove() {
        super.remove();
        if (this.thrower != null) {
            this.thrower.fishingBobber = null;
        }
    }
}
