package io.github.kimovoid.polished.mixin.client.zoom;

import io.github.kimovoid.polished.client.feature.zoom.Zoom;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "getFov", at = @At(value = "RETURN"), cancellable = true)
    public void doZoom(float tickDelta, CallbackInfoReturnable<Float> cir) {
        Zoom.INSTANCE.update();
        float returnValue = Zoom.INSTANCE.getFov(cir.getReturnValue(), tickDelta);
        cir.setReturnValue(returnValue);
    }
}
