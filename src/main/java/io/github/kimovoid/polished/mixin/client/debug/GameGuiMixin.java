package io.github.kimovoid.polished.mixin.client.debug;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.client.PolishedClient;
import io.github.kimovoid.polished.client.feature.debug.DebugScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GameGui;
import net.minecraft.client.options.GameOptions;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameGui.class)
public class GameGuiMixin {

    @Shadow private Minecraft minecraft;

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/options/GameOptions;debugEnabled:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    private boolean renderModernDebugScreen(GameOptions instance, Operation<Boolean> original) {
        if (!PolishedClient.CONFIG.modernDebugScreen.get()) {
            return original.call(instance);
        }

        if (instance.debugEnabled) {
            DebugScreen.INSTANCE.renderLeft(this.minecraft);
            DebugScreen.INSTANCE.renderRight(this.minecraft);
        }
        return false;
    }
}
