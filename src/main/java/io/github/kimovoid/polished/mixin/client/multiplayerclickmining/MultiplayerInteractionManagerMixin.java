package io.github.kimovoid.polished.mixin.client.multiplayerclickmining;

import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.MultiplayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerInteractionManager.class)
public class MultiplayerInteractionManagerMixin {

    @Shadow private int miningCooldown;

    @Inject(method = "stopMiningBlock", at = @At("TAIL"))
    private void resetDelay(CallbackInfo ci) {
        if (PolishedClient.CONFIG.multiplayerClickMining.get()) {
            this.miningCooldown = 0;
        }
    }
}
