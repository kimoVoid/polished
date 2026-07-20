package io.github.kimovoid.polished.mixin.server.properties;

import io.github.kimovoid.polished.server.PolishedServer;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @ModifyConstant(
            method = {
                    "handlePlayerHandAction",
                    "handlePlayerUse"
            },
            constant = @Constant(intValue = 16)
    )
    private int setSpawnProtection(int orig) {
        return PolishedServer.INSTANCE.properties.spawnProtection;
    }
}
