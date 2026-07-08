package io.github.kimovoid.polished.client.feature.debug;

import io.github.kimovoid.polished.client.PolishedClient;
import io.github.kimovoid.polished.client.keybinding.KeyBindingHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.io.PrintWriter;

public class DebugOptions {

    public boolean renderChunkBorders = false;
    public boolean renderHitboxes = false;
    public boolean showAdvancedTooltips = false;

    public void handleDebugKeybinds() {
        int key = Keyboard.getEventKey();
        boolean save = false;
        KeyBindingHandler keybinds = PolishedClient.INSTANCE.keyBindingHandler;
        if (!Keyboard.isKeyDown(keybinds.getKeyFromCode(Keyboard.KEY_F3))) {
            return;
        }
        
        // print help
        if (key == keybinds.getKeyByName("key.debug.help", Keyboard.KEY_Q)) {
            this.printDebugMessage("Key bindings:");
            this.printKeyBinding(Keyboard.KEY_A, "Reload chunks");
            this.printKeyBinding(Keyboard.KEY_B, "Show hitboxes");
            this.printKeyBinding(Keyboard.KEY_D, "Clear chat");
            this.printKeyBinding(Keyboard.KEY_G, "Show chunk borders");
            this.printKeyBinding(Keyboard.KEY_H, "Advanced tooltips");
            this.printKeyBinding(Keyboard.KEY_Q, "Show this list");
            this.printKeyBinding(Keyboard.KEY_S, "Reload resources");
        }

        // reload chunks
        if (key == keybinds.getKeyFromCode(Keyboard.KEY_A)) {
            if (Minecraft.INSTANCE.worldRenderer != null) {
                Minecraft.INSTANCE.worldRenderer.reload();
                this.printDebugMessage("Reloading chunks...");
            }
        }

        // hitboxes
        if (key == keybinds.getKeyFromCode(Keyboard.KEY_B)) {
            save = true;
            this.renderHitboxes = !this.renderHitboxes;
            this.printDebugMessage("Hitboxes: " + (this.renderHitboxes ? "shown" : "hidden"));
        }

        // clear chat
        if (key == keybinds.getKeyFromCode(Keyboard.KEY_D)) {
            Minecraft.INSTANCE.gui.clearChat();
        }

        // chunk borders
        if (key == keybinds.getKeyFromCode(Keyboard.KEY_G)) {
            save = true;
            this.renderChunkBorders = !this.renderChunkBorders;
            this.printDebugMessage("Chunk borders: " + (this.renderChunkBorders ? "shown" : "hidden"));
        }

        // advanced tooltips
        if (key == keybinds.getKeyFromCode(Keyboard.KEY_H)) {
            save = true;
            this.showAdvancedTooltips = !this.showAdvancedTooltips;
            this.printDebugMessage("Advanced tooltips: " + (this.showAdvancedTooltips ? "shown" : "hidden"));
        }

        // added F3+S message too
        if (key == keybinds.getKeyByName("key.debug.reloadResources", Keyboard.KEY_S)) {
            Minecraft.INSTANCE.forceReload();
            this.printDebugMessage("Reloading resources...");
        }

        if (save) {
            Minecraft.INSTANCE.options.save();
        }
    }

    public void load(String[] stringArray) {
        boolean b = stringArray[1].equals("true");
        switch (stringArray[0]) {
            case "renderChunkBorders":
                this.renderChunkBorders = b;
                break;
            case "renderHitboxes":
                this.renderHitboxes = b;
                break;
            case "showAdvancedTooltips":
                this.showAdvancedTooltips = b;
                break;
        }
    }

    public void save(PrintWriter printWriter) {
        printWriter.println("renderChunkBorders:" + this.renderChunkBorders);
        printWriter.println("renderHitboxes:" + this.renderHitboxes);
        printWriter.println("showAdvancedTooltips:" + this.showAdvancedTooltips);
    }

    private void printDebugMessage(String s) {
        Minecraft.INSTANCE.gui.addChatMessage("§e[DEBUG]: §f" + s);
    }

    private void printKeyBinding(int keyCode, String s) {
        KeyBindingHandler keybinds = PolishedClient.INSTANCE.keyBindingHandler;
        int modifier = keybinds.getKeyFromCode(Keyboard.KEY_F3);
        int key = keybinds.getKeyFromCode(keyCode);
        Minecraft.INSTANCE.gui.addChatMessage(String.format("%s + %s = %s",
                Keyboard.getKeyName(modifier),
                Keyboard.getKeyName(key),
                s));
    }
}
