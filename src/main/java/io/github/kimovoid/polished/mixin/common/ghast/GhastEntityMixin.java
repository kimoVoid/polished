package io.github.kimovoid.polished.mixin.common.ghast;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.FlyingMobEntity;
import net.minecraft.entity.mob.monster.GhastEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GhastEntity.class)
public abstract class GhastEntityMixin extends FlyingMobEntity {

    public GhastEntityMixin(World world) {
        super(world);
    }

    @WrapOperation(method = "aiTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/monster/GhastEntity;canSee(Lnet/minecraft/entity/Entity;)Z"))
    private boolean fixGhasts(GhastEntity instance, Entity entity, Operation<Boolean> original) {
        return original.call(instance, entity) && !this.isInWall();
    }
}
