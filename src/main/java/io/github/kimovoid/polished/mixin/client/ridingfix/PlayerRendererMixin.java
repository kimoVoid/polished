package io.github.kimovoid.polished.mixin.client.ridingfix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.mob.player.ClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.model.entity.HumanoidModel;
import net.minecraft.entity.mob.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Shadow public HumanoidModel player;
    @Shadow private HumanoidModel armor1;
    @Shadow private HumanoidModel armor2;

    @Inject(method = "renderRightHand", at = @At(value = "HEAD"))
    private void fixRidingHand(CallbackInfo ci) {
        ClientPlayerEntity clientPlayer = Minecraft.INSTANCE.player;
        this.player.riding = clientPlayer != null && clientPlayer.isRiding();
    }

    @Inject(method = "render(Lnet/minecraft/entity/mob/player/PlayerEntity;DDDFF)V", at = @At("HEAD"))
    private void fixLeggingsModel(PlayerEntity playerEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
        this.armor1.riding = this.armor2.riding = playerEntity.isRiding();
    }

    @Inject(method = "render(Lnet/minecraft/entity/mob/player/PlayerEntity;DDDFF)V", at = @At("TAIL"))
    private void resetLeggingsModel(PlayerEntity playerEntity, double d, double e, double f, float g, float h, CallbackInfo ci) {
        this.armor1.riding = this.armor2.riding = false;
    }
}
