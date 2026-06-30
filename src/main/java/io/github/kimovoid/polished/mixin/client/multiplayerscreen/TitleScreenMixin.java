package io.github.kimovoid.polished.mixin.client.multiplayerscreen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.client.feature.gui.multiplayer.MultiplayerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {

    @WrapOperation(method = "buttonClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", ordinal = 2))
    private void onNewGuiMainMenu(Minecraft instance, Screen screen, Operation<Void> original) {
        instance.openScreen(new MultiplayerScreen(this));
    }
}
