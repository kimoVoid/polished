package io.github.kimovoid.polished.mixin.client.zoom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.client.feature.zoom.Zoom;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/input/Mouse;getEventDWheel()I"
            ),
            remap = false
    )
    public int doZoomScroll(Operation<Integer> original) {
        int amount = Mouse.getEventDWheel();
        if (amount != 0 && Zoom.INSTANCE.scroll(amount)) {
            return 0;
        }
        return amount;
    }
}
