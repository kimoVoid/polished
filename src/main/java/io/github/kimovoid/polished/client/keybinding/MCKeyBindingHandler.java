package io.github.kimovoid.polished.client.keybinding;

import io.github.kimovoid.moderncontrols.ModernControls;
import net.minecraft.client.options.KeyBinding;

public class MCKeyBindingHandler extends KeyBindingHandler {

    @Override
    public int getKeyFromCode(int code) {
        return ModernControls.getKeyCodeByOriginal(code);
    }

    @Override
    public KeyBinding getKeybinding(String name, int keyCode, boolean allowsConflicts) {
        return ModernControls.getCustomKey(name, keyCode, allowsConflicts);
    }
}
