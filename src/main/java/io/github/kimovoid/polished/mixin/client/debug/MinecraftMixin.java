package io.github.kimovoid.polished.mixin.client.debug;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.GameOptions;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow public GameOptions options;
    @Shadow public Screen screen;
    @Unique private boolean showProfiler;

    @WrapOperation(
            method = "run",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/options/GameOptions;debugEnabled:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean replaceProfiler(GameOptions instance, Operation<Boolean> original) {
        if (!PolishedClient.CONFIG.modernDebugScreen.get()) {
            return original.call(instance);
        }

        return original.call(instance) && this.showProfiler;
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/options/GameOptions;debugEnabled:Z",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER
            )
    )
    private void toggleProfiler(CallbackInfo ci) {
        if (!PolishedClient.CONFIG.modernDebugScreen.get())
            return;

        if (!this.options.debugEnabled) {
            this.showProfiler = false;
            return;
        }

        this.showProfiler = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
    }

    @Inject(method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/mob/player/ClientPlayerEntity;handleKeyEvent(IZ)V",
                    shift = At.Shift.AFTER
            )
    )
    public void handleDebugKeybinds(CallbackInfo ci) {
        if (this.screen != null || !Keyboard.getEventKeyState()) {
            return;
        }

        PolishedClient.INSTANCE.debugOptions.handleDebugKeybinds();
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;forceReload()V"))
    private void invalidateForceReload(Minecraft instance, Operation<Void> original) {
    }
}
