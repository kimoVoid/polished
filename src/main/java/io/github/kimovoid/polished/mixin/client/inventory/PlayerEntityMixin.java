package io.github.kimovoid.polished.mixin.client.inventory;

import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.mob.player.LocalClientPlayerEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LocalClientPlayerEntity.class, PlayerEntity.class})
public class PlayerEntityMixin {

    @Inject(method = "dropItem()V", at = @At("HEAD"), cancellable = true)
    private void inventoryTweaks_dropSelectedItem(CallbackInfo ci) {
        if (!PolishedClient.CONFIG.ctrlDropStack.get()) {
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            Minecraft.INSTANCE.interactionManager.clickSlot(0, 36 + Minecraft.INSTANCE.player.inventory.selectedSlot, 0, false, Minecraft.INSTANCE.player);
            Minecraft.INSTANCE.interactionManager.clickSlot(0, -999, 0, false, Minecraft.INSTANCE.player);
            ci.cancel();
        }
    }
}
