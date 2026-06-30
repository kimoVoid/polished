package io.github.kimovoid.polished.mixin.client.misc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.client.feature.gui.CallbackButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;buttonClicked(Lnet/minecraft/client/gui/widget/ButtonWidget;)V"))
    private void onActionPerformed(Screen screen, ButtonWidget button, Operation<Void> original) {
        if (button instanceof CallbackButtonWidget) {
            CallbackButtonWidget buttonWidget = (CallbackButtonWidget) button;
            buttonWidget.onPress();
            return;
        }

        original.call(screen, button);
    }
}
