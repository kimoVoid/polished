package io.github.kimovoid.polished.mixin.server.sync.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.server.PolishedServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.FlyingMobEntity;
import net.minecraft.entity.mob.monster.GhastEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.SERVER)
@Mixin(GhastEntity.class)
public abstract class GhastEntityMixin extends FlyingMobEntity {

    public GhastEntityMixin(World world) {
        super(world);
    }

    @WrapOperation(method = "aiTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Ljava/lang/String;FF)V"))
    private void redirectGhastEvent(World instance, Entity source, String sound, float volume, float pitch, Operation<Void> original) {
        PolishedServer.INSTANCE.redirectSoundEvent(instance, this.x, this.y, this.z, sound, volume, pitch, null);
    }
}
