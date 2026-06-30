package io.github.kimovoid.polished.mixin.client.ghosttnt;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.TntBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(TntBlock.class)
public class TntBlockMixin {

    @Inject(method = "onExploded", at = @At("HEAD"), cancellable = true)
    private void fixGhostTnt(World world, int x, int y, int z, CallbackInfo ci) {
        if (world.isMultiplayer) {
            ci.cancel();
        }
    }
}
