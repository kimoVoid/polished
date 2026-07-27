package io.github.kimovoid.polished.mixin.server.sync.fishingrod;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FishingBobberEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.network.packet.AddEntityPacket;
import net.minecraft.server.TrackedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrackedEntity.class)
public class TrackedEntityMixin {

    @WrapOperation(method = "createAddEntityPacket", at = @At(value = "NEW", target = "(Lnet/minecraft/entity/Entity;I)Lnet/minecraft/network/packet/AddEntityPacket;", ordinal = 4))
    private AddEntityPacket setFishingBobberData(Entity entity, int type, Operation<AddEntityPacket> original) {
        PlayerEntity playerEntity = ((FishingBobberEntity)entity).thrower;
        return new AddEntityPacket(entity, type, playerEntity != null ? playerEntity.networkId : 0);
    }
}
