package io.github.kimovoid.polished.mixin.client.autosave;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Environment(EnvType.CLIENT)
@Mixin(World.class)
public class WorldMixin {

    @ModifyConstant(
            method = "<init>*",
            constant = @Constant(intValue = 40)
    )
    private int extendAutoSaveTime(int constant) {
        return 6000;
    }
}
