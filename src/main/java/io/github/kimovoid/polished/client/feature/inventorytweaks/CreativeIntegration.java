package io.github.kimovoid.polished.client.feature.inventorytweaks;

import io.github.kimovoid.creative.client.gui.CreativeInventoryScreen;
import io.github.kimovoid.creative.tab.CreativeModeTab;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;

public class CreativeIntegration {

    public static int getSlot(InventoryMenuScreen screen, int slot) {
        if (screen instanceof CreativeInventoryScreen
                && ((CreativeInventoryScreen)screen).getSelectedTab() == CreativeModeTab.INVENTORY.getId()) {
            return slot - 1;
        }
        return slot;
    }

    public static boolean shouldSkipSlot(InventoryMenuScreen screen, int slot) {
        if (screen instanceof CreativeInventoryScreen
                && ((CreativeInventoryScreen)screen).getSelectedTab() != CreativeModeTab.INVENTORY.getId()) {
            return slot < screen.menu.slots.size() - 9;
        }
        return false;
    }
}
