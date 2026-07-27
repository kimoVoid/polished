package io.github.kimovoid.polished.mixin.server.sync.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kimovoid.polished.server.PolishedServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.ButtonBlock;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.SERVER)
@Mixin(ButtonBlock.class)
public class ButtonBlockMixin {

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLjava/lang/String;FF)V"))
    private void redirectUseSound(World instance, double x, double y, double z, String sound, float volume, float pitch, Operation<Void> original, @Local(argsOnly = true) PlayerEntity player) {
        PolishedServer.INSTANCE.redirectSoundEvent(instance, x, y, z, sound, volume, pitch, player);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLjava/lang/String;FF)V"))
    private void redirectTickSound(World instance, double x, double y, double z, String sound, float volume, float pitch, Operation<Void> original) {
        PolishedServer.INSTANCE.redirectSoundEvent(instance, x, y, z, sound, volume, pitch, null);
    }
}
