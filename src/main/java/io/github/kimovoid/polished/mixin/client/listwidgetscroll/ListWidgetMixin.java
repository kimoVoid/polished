package io.github.kimovoid.polished.mixin.client.listwidgetscroll;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ListWidget;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ListWidget.class)
public class ListWidgetMixin {

    @Shadow @Final private Minecraft minecraft;
    @Shadow private float mouseYStart;
    @Shadow private float scrollAmount;
    @Shadow @Final protected int entryHeight;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;isButtonDown(I)Z"))
    private void addScrollWheelScrolling(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        if (!Mouse.isButtonDown(0)) {
            for (; Mouse.next(); this.minecraft.screen.handleMouse()) {
                int wheel = Mouse.getEventDWheel();
                if (wheel != 0) {
                    wheel = wheel > 0 ? -1 : 1;
                    this.scrollAmount += (float) (wheel * this.entryHeight) / 2;
                }
            }
            this.mouseYStart = -1.0F;
        }
    }
}
