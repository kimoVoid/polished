package io.github.kimovoid.polished.mixin.server.sync.sound;

import io.github.kimovoid.polished.server.PolishedServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.TntBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(TntBlock.class)
public class TntBlockMixin {

    @Inject(method = "onBroken", at = @At("TAIL"))
    private void playTntSound(World world, int x, int y, int z, int metadata, CallbackInfo ci) {
        if (metadata != 1) {
            return;
        }
        PolishedServer.INSTANCE.redirectSoundEvent(world, x + 0.5, y + 0.5, z + 0.5, "random.fuse", 1.0F, 1.0F, null);
    }
}
