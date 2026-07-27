package io.github.kimovoid.polished.mixin.server.sync.entitymovement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.TrackedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TrackedEntity.class)
public class TrackedEntityMixin {

    @Shadow public Entity entity;

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 8))
    private int test(int constant) {
        // send constant changes to boat and player movements
        // and more entity movement updates overall
        return (this.entity instanceof BoatEntity boat && boat.rider != null)
                || this.entity instanceof PlayerEntity ? 0 : 4;
    }
}
