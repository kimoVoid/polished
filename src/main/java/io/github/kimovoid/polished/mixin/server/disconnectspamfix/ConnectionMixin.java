package io.github.kimovoid.polished.mixin.server.disconnectspamfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.SERVER)
@Mixin(Connection.class)
public class ConnectionMixin {

    @WrapOperation(method = "handleException", at = @At(value = "INVOKE", target = "Ljava/lang/Exception;printStackTrace()V"))
    private void removeExceptionSpam(Exception instance, Operation<Void> original) {
    }
}
