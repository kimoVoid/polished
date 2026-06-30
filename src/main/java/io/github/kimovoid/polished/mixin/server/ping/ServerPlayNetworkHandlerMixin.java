package io.github.kimovoid.polished.mixin.server.ping;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.server.PolishedServer;
import io.github.kimovoid.polished.server.feature.ping.PingHostHandler;
import io.github.kimovoid.polished.server.feature.ping.PlayerPing;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketHandler;
import net.minecraft.network.packet.PingHostPacket;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements PingHostHandler {

	@Shadow private ServerPlayerEntity player;
	@Shadow public Connection connection;

	@Unique private int currentTick;
	@Unique private long pingHostTimeSent;
	@Unique private boolean receivedPingHost = true;
	@Unique private int lastPlayerInfoPacket;

	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/Connection;setListener(Lnet/minecraft/network/PacketHandler;)V"
			)
	)
	private void redirect(Connection instance, PacketHandler listener, Operation<Void> original) {
		if (connection != null) original.call(instance, listener);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	public void injectTick(CallbackInfo ci) {
		this.currentTick++;
		this.lastPlayerInfoPacket++;

		if (this.lastPlayerInfoPacket >= 60) {
			PolishedServer.INSTANCE.sendPlayerInfo(this.player.name, true, ((PlayerPing)this.player).getPing());
			this.lastPlayerInfoPacket = 0;
		}

		if (this.currentTick >= 20) {
			this.currentTick = 0;
			if (!this.receivedPingHost) {
				return;
			}

			this.receivedPingHost = false;
			this.pingHostTimeSent = System.currentTimeMillis();
			this.connection.send(new PingHostPacket());
		}
	}

	@Inject(method = "onDisconnect(Ljava/lang/String;[Ljava/lang/Object;)V", at = @At("HEAD"))
	public void removeFromTab(CallbackInfo ci) {
		PolishedServer.INSTANCE.sendPlayerInfo(this.player.name, false, 0);
	}

	@Override
	public void handlePingHost(PingHostPacket packet) {
		int ping = (int) (System.currentTimeMillis() - this.pingHostTimeSent);
		((PlayerPing)this.player).setPing(Math.max(ping, 0));
		this.receivedPingHost = true;
	}
}
