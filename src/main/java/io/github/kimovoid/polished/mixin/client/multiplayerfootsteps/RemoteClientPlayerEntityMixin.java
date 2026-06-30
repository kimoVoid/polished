package io.github.kimovoid.polished.mixin.client.multiplayerfootsteps;

import net.minecraft.block.Block;
import net.minecraft.client.entity.mob.player.RemoteClientPlayerEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RemoteClientPlayerEntity.class)
public abstract class RemoteClientPlayerEntityMixin extends PlayerEntity {

    @Unique private int distanceOnNextBlock2;

    public RemoteClientPlayerEntityMixin(World world) {
        super(world);
    }

    @Inject(
            method = "mobTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/entity/mob/player/RemoteClientPlayerEntity;lastBob:F",
                    opcode = Opcodes.PUTFIELD
            )
    )
    private void fixRemotePlayerFootsteps(CallbackInfo ci) {
        boolean sneaking = this.isSneaking();
        double veloX = this.x - this.prevX;
        double veloZ = this.z - this.prevZ;

        if (this.makesSteps() && !sneaking && this.vehicle == null) {
            this.walkDistance = (float)((double)this.walkDistance + (double)MathHelper.sqrt(veloX * veloX + veloZ * veloZ) * 0.6D);
            int x = MathHelper.floor(this.x);
            int y = MathHelper.floor(this.y - (double)0.2F - (double)this.eyeHeight);
            int z = MathHelper.floor(this.z);
            int blockId = this.world.getBlock(x, y, z);
            if (this.world.getBlock(x, y - 1, z) == Block.FENCE.id) {
                blockId = this.world.getBlock(x, y - 1, z);
            }

            if (this.walkDistance > (float)this.distanceOnNextBlock2 && blockId > 0) {
                this.distanceOnNextBlock2 = MathHelper.floor(this.walkDistance) + 1;
                Block.Sounds sound = Block.BY_ID[blockId].sounds;
                if (this.world.getBlock(x, y + 1, z) == Block.SNOW_LAYER.id) {
                    sound = Block.SNOW_LAYER.sounds;
                    this.world.playSound(this, sound.getStepping(), sound.getVolume() * 0.15F, sound.getPitch());
                } else if (!Block.BY_ID[blockId].material.isLiquid()) {
                    this.world.playSound(this, sound.getStepping(), sound.getVolume() * 0.15F, sound.getPitch());
                }

                Block.BY_ID[blockId].onSteppedOn(this.world, x, y, z, this);
            }
        }
    }
}
