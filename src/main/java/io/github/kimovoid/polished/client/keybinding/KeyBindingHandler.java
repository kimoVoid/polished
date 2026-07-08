package io.github.kimovoid.polished.client.keybinding;

import net.minecraft.client.Minecraft;
import net.minecraft.client.options.KeyBinding;

public class KeyBindingHandler {

    /* Dummy method for Modern Controls support */
    public int getKeyFromCode(int code) {
        return code;
    }

    /* Returns the code back if no keybind was found */
    public int getKeyByName(String name, int code) {
        for (KeyBinding key : Minecraft.INSTANCE.options.keyBindings) {
            if (key.name.equals(name)) code = key.keyCode;
        }
        return code;
    }

    public KeyBinding getKeybinding(String name, int keyCode, boolean allowsConflicts) {
        return new KeyBinding(name, keyCode);
    }
}
