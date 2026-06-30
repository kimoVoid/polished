package io.github.kimovoid.polished.mixin.server.ping;

import io.github.kimovoid.polished.server.feature.ping.PlayerPing;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements PlayerPing {

	@Unique
	private int ping;

	public int getPing() {
		return this.ping;
	}

	public void setPing(int ping) {
		this.ping = ping;
	}
}
