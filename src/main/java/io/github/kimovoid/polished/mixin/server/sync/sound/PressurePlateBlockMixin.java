package io.github.kimovoid.polished.mixin.server.sync.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.server.PolishedServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.SERVER)
@Mixin(PressurePlateBlock.class)
public class PressurePlateBlockMixin {

    @WrapOperation(method = "updateOutputState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLjava/lang/String;FF)V"))
    private void redirectSound(World instance, double x, double y, double z, String sound, float volume, float pitch, Operation<Void> original) {
        PolishedServer.INSTANCE.redirectSoundEvent(instance, x, y, z, sound, volume, pitch, null);
    }
}
