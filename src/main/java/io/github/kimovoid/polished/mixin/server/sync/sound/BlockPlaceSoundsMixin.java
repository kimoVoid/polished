package io.github.kimovoid.polished.mixin.server.sync.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kimovoid.polished.server.PolishedServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.PlaceableItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.SERVER)
@Mixin(value = {PlaceableItem.class, BlockItem.class, HoeItem.class})
public class BlockPlaceSoundsMixin {

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(DDDLjava/lang/String;FF)V"))
    private void redirectSound(World instance, double x, double y, double z, String sound, float volume, float pitch, Operation<Void> original, @Local(argsOnly = true) PlayerEntity player) {
        PolishedServer.INSTANCE.redirectSoundEvent(instance, x, y, z, sound, volume, pitch, player);
    }
}
