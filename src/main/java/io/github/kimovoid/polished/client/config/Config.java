package io.github.kimovoid.polished.client.config;

import net.ornithemc.osl.config.api.ConfigScope;
import net.ornithemc.osl.config.api.LoadingPhase;
import net.ornithemc.osl.config.api.config.BaseConfig;
import net.ornithemc.osl.config.api.config.option.BooleanOption;
import net.ornithemc.osl.config.api.config.option.FloatOption;
import net.ornithemc.osl.config.api.serdes.FileSerializerType;
import net.ornithemc.osl.config.api.serdes.SerializerTypes;

public class Config extends BaseConfig {

    // general
    public final BooleanOption modernDebugScreen = new BooleanOption("modern-debug-screen", "A modern look for the F3 screen with more info.", true);
    public final BooleanOption multiplayerClickMining = new BooleanOption("multiplayer-click-mining", "Allows you to click-mine blocks in multiplayer.", false);
    public final BooleanOption frontPerspective = new BooleanOption("front-perspective", "Front-facing perspective with F5.", true);

    // inventory tweaks
    public final BooleanOption enableInventoryTweaks = new BooleanOption("inventory-tweaks", "Disabling this will disable all inventory tweaks.", true);
    public final BooleanOption dragGraphics = new BooleanOption("drag-graphics", "", true);
    public final BooleanOption leftClickDrag = new BooleanOption("dragging", "", true);
    public final BooleanOption preferShiftLMB = new BooleanOption("prefer-shift-lmb", "", true);
    public final BooleanOption preferShiftRMB = new BooleanOption("prefer-shift-rmb", "", true);
    public final BooleanOption dropKeyInv = new BooleanOption("drop-key-inv", "", true);
    public final BooleanOption ctrlDropStack = new BooleanOption("drop-stack-key", "", true);
    public final BooleanOption hotkeySwap = new BooleanOption("hotkey-swap", "", true);
    public final BooleanOption craftAll = new BooleanOption("craft-all", "", true);
    public final BooleanOption shiftIntoArmor = new BooleanOption("shift-armor", "", true);
    public final BooleanOption shiftIntoFurnace = new BooleanOption("shift-furnace", "", true);
    public final BooleanOption shiftClickAnyLMB = new BooleanOption("shift-click-any-lmb", "", true);
    public final BooleanOption tweakRMB = new BooleanOption("tweak-rmb", "Drag items over the same slots", false);
    public final BooleanOption tweakLMBPickUp = new BooleanOption("tweak-lmb-pickup", "", true);
    public final BooleanOption tweakLMBShiftClick = new BooleanOption("tweak-shift-lmb", "", true);
    public final BooleanOption doubleClick = new BooleanOption("double-click", "", true);

    // zoom
    public final FloatOption zoomDivisor = new FloatOption("zoom-divisor", "The amount of zoom", 4F, value -> value >= 1F && value <= 16F);
    public final FloatOption zoomSpeed = new FloatOption("zoom-speed", "The speed of the zoom animation", 7.5F, value -> value >= 1F && value <= 10F);
    public final BooleanOption zoomScrolling = new BooleanOption("zoom-scroll", "Allow scrolling while zooming", true);
    public final BooleanOption decreaseSensitivity = new BooleanOption("zoom-decrease-sens", "Decrease mouse sensitivity while zooming", false);
    public final BooleanOption smoothCamera = new BooleanOption("zoom-smooth-camera", "Enable smooth camera while zooming", true);

    @Override
    public String getNamespace() {
        return "polished";
    }

    @Override
    public String getName() {
        return "Polished Settings";
    }

    @Override
    public String getSaveName() {
        return "polished.json";
    }

    @Override
    public ConfigScope getScope() {
        return ConfigScope.GLOBAL;
    }

    @Override
    public LoadingPhase getLoadingPhase() {
        return LoadingPhase.START;
    }

    @Override
    public FileSerializerType<?> getType() {
        return SerializerTypes.JSON;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void init() {
        this.registerOptions("general",
                modernDebugScreen,
                multiplayerClickMining,
                frontPerspective
        );

        this.registerOptions("inventory",
                enableInventoryTweaks,
                dragGraphics,
                leftClickDrag,
                preferShiftLMB,
                preferShiftRMB,
                dropKeyInv,
                ctrlDropStack,
                hotkeySwap,
                craftAll,
                shiftIntoArmor,
                shiftIntoFurnace,
                shiftClickAnyLMB,
                tweakLMBShiftClick,
                tweakRMB,
                tweakLMBPickUp,
                doubleClick
        );

        this.registerOptions("zoom",
                zoomDivisor,
                zoomSpeed,
                zoomScrolling,
                decreaseSensitivity,
                smoothCamera
        );
    }
}
