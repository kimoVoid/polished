package io.github.kimovoid.polished.mixin.server.properties;

import io.github.kimovoid.polished.server.PolishedServer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow public MinecraftServer server;

    public ServerPlayerEntityMixin(World world) {
        super(world);
    }

    @Inject(method = "die", at = @At("TAIL"))
    public void showDeathCoords(Entity killer, CallbackInfo ci) {
        if (PolishedServer.INSTANCE.properties.deathCoordinates) {
            this.server.playerManager.sendMessageToPlayer(
                    this.name,
                    String.format("Death coordinates: %.1f, %.1f, %.1f", this.x, this.y, this.z)
            );
        }
    }
}
