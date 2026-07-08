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
import net.ornithemc.osl.keybinds.api.KeybindEvents;
import net.ornithemc.osl.keybinds.api.KeybindRegistry;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import org.lwjgl.input.Keyboard;

import java.util.LinkedHashMap;

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

		KeybindEvents.REGISTER_KEYBINDS.register(() -> {
			KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.playerlist", Keyboard.KEY_TAB, false), "key.categories.multiplayer");
			KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.zoom", Keyboard.KEY_C, false), "key.categories.gameplay");

			if (hasModernControls) {
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.modifier", Keyboard.KEY_F3, true), "key.categories.debug");
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.reloadChunks", Keyboard.KEY_A, true), "key.categories.debug");
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.hitboxes", Keyboard.KEY_B, true), "key.categories.debug");
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.clearChat", Keyboard.KEY_D, true), "key.categories.debug");
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.chunkBorders", Keyboard.KEY_G, true), "key.categories.debug");
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.tooltips", Keyboard.KEY_H, true), "key.categories.debug");
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.help", Keyboard.KEY_Q, true), "key.categories.debug");
				KeybindRegistry.register(this.keyBindingHandler.getKeybinding("key.debug.reloadResources", Keyboard.KEY_S, true), "key.categories.debug");
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
