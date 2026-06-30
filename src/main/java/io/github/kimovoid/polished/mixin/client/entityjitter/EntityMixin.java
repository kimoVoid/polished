package io.github.kimovoid.polished.mixin.client.entityjitter;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class EntityMixin {

    @WrapOperation(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;x:D", opcode = Opcodes.PUTFIELD))
    private void fixX(Entity entity, double value, Operation<Void> original) {
        if (!entity.world.isMultiplayer || entity instanceof PlayerEntity || !(entity instanceof MobEntity)) {
            entity.x = value;
        }
    }

    @WrapOperation(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;y:D", opcode = Opcodes.PUTFIELD))
    private void fixY(Entity entity, double value, Operation<Void> original) {
        if (!entity.world.isMultiplayer || entity instanceof PlayerEntity || !(entity instanceof MobEntity)) {
            entity.y = value;
        }
    }

    @WrapOperation(method = "move", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;z:D", opcode = Opcodes.PUTFIELD))
    private void fixZ(Entity entity, double value, Operation<Void> original) {
        if (!entity.world.isMultiplayer || entity instanceof PlayerEntity || !(entity instanceof MobEntity)) {
            entity.z = value;
        }
    }
}
