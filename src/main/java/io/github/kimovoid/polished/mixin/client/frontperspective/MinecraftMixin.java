package io.github.kimovoid.polished.mixin.client.frontperspective;

import io.github.kimovoid.polished.client.PolishedClient;
import io.github.kimovoid.polished.client.feature.frontperspective.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin implements IMinecraft {

    @Unique public int thirdPersonMode = 0;
    @Shadow public Screen screen;
    @Shadow public GameOptions options;

    @Inject(method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/mob/player/ClientPlayerEntity;handleKeyEvent(IZ)V",
                    shift = At.Shift.AFTER
            )
    )
    public void handlePerspectiveKey(CallbackInfo ci) {
        if (this.screen != null
                || !Keyboard.getEventKeyState()
                || !PolishedClient.CONFIG.frontPerspective.get()) {
            return;
        }

        if (Keyboard.getEventKey() == PolishedClient.INSTANCE.keyBindingHandler.getKeyFromCode(Keyboard.KEY_F5)) {
            this.thirdPersonMode++;
            this.options.perspective = false;
            if (thirdPersonMode > 2) {
                this.thirdPersonMode = 0;
                this.options.perspective = true;
            }
        }
    }

    @Override
    public int getThirdPersonMode() {
        return this.thirdPersonMode;
    }
}