package io.github.kimovoid.polished.mixin.common.rain;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldData;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(World.class)
public class WorldMixin {

	@Shadow protected WorldData data;
	@Final @Shadow public Dimension dimension;

	@WrapOperation(
			method = "saveData()V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/storage/WorldStorage;saveData(Lnet/minecraft/world/WorldData;Ljava/util/List;)V"
			)
	)
	private void fixWeatherSave(WorldStorage storage, WorldData worldData, List<PlayerEntity> playerEntities, Operation<Void> original) {
		if (this.dimension.id == 0) original.call(storage, data, playerEntities);
	}
}
