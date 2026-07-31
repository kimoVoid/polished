package io.github.kimovoid.polished.mixin.client.sync;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity {

    public BoatEntityMixin(World world) {
        super(world);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/World;isMultiplayer:Z",
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            )
    )
    private void playBoatParticles(CallbackInfo ci) {
        if (!this.world.isMultiplayer) {
            return;
        }

        double speed = Math.sqrt(this.velocityX * this.velocityX + this.velocityZ * this.velocityZ);
        if (speed > 0.15) {
            double p18 = Math.cos(this.yaw * Math.PI / 180.0);
            double s23 = Math.sin(this.yaw * Math.PI / 180.0);

            for (int o25 = 0; o25 < 1.0 + speed * 60.0; o25++) {
                double v = this.random.nextFloat() * 2.0F - 1.0F;
                double x = (this.random.nextInt(2) * 2 - 1) * 0.7;
                if (this.random.nextBoolean()) {
                    double y = this.x - p18 * v * 0.8 + s23 * x;
                    double a = this.z - s23 * v * 0.8 - p18 * x;
                    this.world.addParticle("splash", y, this.y - 0.125, a, this.velocityX, this.velocityY, this.velocityZ);
                } else {
                    double z = this.x + p18 + s23 * v * 0.7;
                    double b = this.z + s23 - p18 * v * 0.7;
                    this.world.addParticle("splash", z, this.y - 0.125, b, this.velocityX, this.velocityY, this.velocityZ);
                }
            }
        }
    }
}
