package io.github.kimovoid.polished.mixin.server.bowsounds;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.SERVER)
@Mixin(BowItem.class)
public class BowItemMixin {

    @WrapOperation(method = "startUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;Ljava/lang/String;FF)V"))
    private void fixBowSound(World instance, Entity source, String sound, float volume, float pitch, Operation<Void> original) {
        instance.doEvent((PlayerEntity) source, 1002, MathHelper.floor(source.x), MathHelper.floor(source.y - (double) source.height), MathHelper.floor(source.z), 0);
    }
}
