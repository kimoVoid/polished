package io.github.kimovoid.polished.mixin.client.multiplayerfootsteps;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class EntityMixin {

    @Shadow public World world;
    @Shadow public double x;
    @Shadow public double prevX;
    @Shadow public double z;
    @Shadow public double prevZ;

    @Shadow private int blocksWalkedOn;
    @Shadow public float walkDistance;

    @ModifyArg(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;sqrt(D)F"
            )
    )
    private double fixMultiplayerVelocity(double orig) {
        if (this.world.isMultiplayer) {
            double x = this.x - this.prevX;
            double z = this.z - this.prevZ;
            return x * x + z * z;
        }
        return orig;
    }

    @WrapOperation(
            method = "move",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/Entity;blocksWalkedOn:I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void fixDistance(Entity instance, int value, Operation<Void> original) {
        this.blocksWalkedOn = MathHelper.floor(this.walkDistance) + 1;
    }
}
