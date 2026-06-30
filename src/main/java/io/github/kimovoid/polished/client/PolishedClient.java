package io.github.kimovoid.polished.client;

import io.github.kimovoid.polished.Polished;
import io.github.kimovoid.polished.client.config.Config;
import io.github.kimovoid.polished.client.feature.debug.DebugOptions;
import io.github.kimovoid.polished.client.feature.debug.DebugScreen;
import io.github.kimovoid.polished.client.feature.zoom.Zoom;
import io.github.kimovoid.polished.client.keybinding.KeyBindingHandler;
import io.github.kimovoid.polished.client.keybinding.MCKeyBindingHandler;
import io.github.kimovoid.polished.networking.PlayerInfoPayload;
import net.fabricmc.loader.api.FabricLoader;
import net.ornithemc.osl.config.api.ConfigManager;
import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import org.lwjgl.input.Keyboard;

import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

public class PolishedClient implements ClientModInitializer {

	public static PolishedClient INSTANCE;
	public static Config CONFIG;

	public KeyBindingHandler keyBindingHandler;
	public DebugOptions debugOptions;
	public LinkedHashMap<String, Integer> playerList = new LinkedHashMap<>();

	@Override
	public void initClient() {
		INSTANCE = this;
		CONFIG = new Config();
		ConfigManager.register(CONFIG);
		DebugScreen.INSTANCE.init();

		this.debugOptions = new DebugOptions();
		this.keyBindingHandler = new KeyBindingHandler();
		boolean hasModernControls = FabricLoader.getInstance().isModLoaded("moderncontrols");
		if (hasModernControls) {
			this.keyBindingHandler = new MCKeyBindingHandler();
		}

		MinecraftClientEvents.TICK_START.register(mc -> Zoom.INSTANCE.tick());
		MinecraftClientEvents.READY_WORLD.register(mc -> this.playerList.clear());

		KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> {
			registry.register(this.keyBindingHandler.getKeybinding("key.playerlist", Keyboard.KEY_TAB, "key.categories.multiplayer", true));
			registry.register(this.keyBindingHandler.getKeybinding("key.zoom", Keyboard.KEY_C, "key.categories.gameplay", true));

			if (hasModernControls) {
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.modifier", Keyboard.KEY_F3, "key.categories.debug", true));
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.reloadChunks", Keyboard.KEY_A, "key.categories.debug", true));
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.hitboxes", Keyboard.KEY_B, "key.categories.debug", true));
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.clearChat", Keyboard.KEY_D, "key.categories.debug", true));
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.chunkBorders", Keyboard.KEY_G, "key.categories.debug", true));
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.tooltips", Keyboard.KEY_H, "key.categories.debug", true));
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.help", Keyboard.KEY_Q, "key.categories.debug", true));
				registry.register(this.keyBindingHandler.getKeybinding("key.debug.reloadResources", Keyboard.KEY_S, "key.categories.debug", true));
			}
		});

		ClientPlayNetworking.registerListener(Polished.PLAYER_INFO_CHANNEL, PlayerInfoPayload::new, (context, payload) -> {
			context.ensureOnMainThread();
			if (payload.isOnline) {
				this.playerList.put(payload.username, payload.ping);
			} else {
				this.playerList.remove(payload.username);
			}
		});
	}
}
