package io.github.kimovoid.polished.mixin.server.properties;

import io.github.kimovoid.polished.server.PolishedServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Environment(EnvType.SERVER)
@Mixin(World.class)
public class WorldMixin {

    @Shadow private boolean allPlayersSleeping;
    @Shadow public List<PlayerEntity> players;

    @Inject(method = "updatePlayersSleepingStatus", at = @At("HEAD"), cancellable = true)
    public void setSleepingPercentage(CallbackInfo ci) {
        int playersSleepingPercentage = PolishedServer.INSTANCE.properties.playerSleepingPercentage;
        if (playersSleepingPercentage == -1) {
            this.allPlayersSleeping = false;
            ci.cancel();
            return;
        }

        if (playersSleepingPercentage < 100 && !this.players.isEmpty()) {
            int minEepers = (int) Math.ceil(this.players.size() * (playersSleepingPercentage / 100d));
            int eepers = 0;
            for (PlayerEntity player : this.players)
                if (player.isSleeping()) {
                    eepers++;
                }

            this.allPlayersSleeping = eepers >= minEepers;
            ci.cancel();
        }
    }

    @Inject(method = "canSleepThroughNights", at = @At("HEAD"), cancellable = true)
    private void fixLoop(CallbackInfoReturnable<Boolean> cir) {
        for (PlayerEntity pl : this.players) {
            if (!pl.isSleeping()) continue;
            if (pl.isSleepingLongEnough()) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }

    @ModifyArg(
            index = 1,
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/NaturalSpawner;spawnMonstersAndWakeUpPlayers(Lnet/minecraft/world/World;Ljava/util/List;)Z"
            )
    )
    private List<PlayerEntity> fixNightmares(List<PlayerEntity> players) {
        players.removeIf(p -> !p.isSleeping());
        return players;
    }
}
