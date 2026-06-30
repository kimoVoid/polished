package io.github.kimovoid.polished.mixin.server.bowsounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.monster.MonsterEntity;
import net.minecraft.entity.mob.monster.SkeletonEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.SERVER)
@Mixin(SkeletonEntity.class)
public abstract class SkeletonEntityMixin extends MonsterEntity {

    public SkeletonEntityMixin(World world) {
        super(world);
    }

    @WrapOperation(method = "targetInSight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Ljava/lang/String;FF)V"))
    private void fixBowSound(World instance, Entity source, String sound, float volume, float pitch, Operation<Void> original) {
        this.world.doEvent(1002, MathHelper.floor(this.x), MathHelper.floor(this.y - (double)this.height), MathHelper.floor(this.z), 0);
    }
}
