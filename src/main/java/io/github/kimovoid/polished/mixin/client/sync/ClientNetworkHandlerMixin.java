package io.github.kimovoid.polished.mixin.client.sync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kimovoid.polished.client.feature.sync.FishingBobberSync;
import net.minecraft.client.network.handler.ClientNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FishingBobberEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.network.packet.AddEntityPacket;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientNetworkHandler.class)
public abstract class ClientNetworkHandlerMixin {

    @Shadow protected abstract Entity getEntity(int networkId);

    @WrapOperation(method = "handleAddEntity", at = @At(value = "NEW", target = "(Lnet/minecraft/world/World;DDD)Lnet/minecraft/entity/FishingBobberEntity;"))
    private FishingBobberEntity setThrower(World world, double x, double y, double z, Operation<FishingBobberEntity> original, @Local(argsOnly = true) AddEntityPacket packet) {
        FishingBobberEntity entity = original.call(world, x, y, z);

        if (packet.data > 0 && this.getEntity(packet.data) instanceof PlayerEntity player) {
            ((FishingBobberSync)entity).setThrower(player);
            packet.data = 0;
        }

        return entity;
    }
}
