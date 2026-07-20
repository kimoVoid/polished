package io.github.kimovoid.polished.mixin.server.properties;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.server.PolishedServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @WrapOperation(
            method = "loadWorld",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/server/world/ServerWorld;difficulty:I",
                    opcode = Opcodes.PUTFIELD
            )
    )
    public void setDifficulty(ServerWorld instance, int value, Operation<Void> original) {
        original.call(instance, PolishedServer.INSTANCE.properties.difficulty);
    }
}
