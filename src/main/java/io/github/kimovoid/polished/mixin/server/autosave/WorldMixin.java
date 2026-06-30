package io.github.kimovoid.polished.mixin.server.autosave;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Environment(EnvType.SERVER)
@Mixin(World.class)
public class WorldMixin {

    @ModifyConstant(
            method = "<init>*",
            constant = @Constant(intValue = 40)
    )
    private int extendAutoSaveTime(int constant) {
        // smaller delay for server worlds since they don't really suffer
        // from auto-save lag spikes. but i still think 2s is too excessive.
        return 600;
    }
}
