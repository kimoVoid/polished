package io.github.kimovoid.polished.client.feature.zoom;

import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.SmoothUtil;
import org.lwjgl.input.Keyboard;

public class Zoom {

    public static final Zoom INSTANCE = new Zoom();

    public boolean active = false;
    private float originalSensitivity;
    private boolean originalSmoothCamera;
    private float targetFactor = 1;
    private float divisor;
    private float lastAnimatedFactor = 1;
    private float animatedFactor = 1;

    public float getFov(float current, float tickDelta) {
        return current * (PolishedClient.CONFIG.zoomSpeed.get() == 10 ? targetFactor : this.lerp(tickDelta, lastAnimatedFactor, animatedFactor));
    }

    public void update() {
        if (shouldStart()) {
            start();
        } else if (shouldStop()) {
            stop();
        }
    }

    private boolean shouldStart() {
        return keyHeld() && !active;
    }

    private void start() {
        active = true;
        setDivisor(PolishedClient.CONFIG.zoomDivisor.get());
        setOptions();
    }

    private boolean shouldStop() {
        return !keyHeld() && active;
    }

    private void stop() {
        active = false;
        targetFactor = 1;
        restoreOptions();
    }

    private boolean keyHeld() {
        return Minecraft.INSTANCE.screen == null && Keyboard.isKeyDown(PolishedClient.INSTANCE.keyBindingHandler.getKeyByName("key.zoom", Keyboard.KEY_C));
    }

    private void setDivisor(float value) {
        divisor = value;
        targetFactor = 1F / value;
    }

    public void setOptions() {
        originalSensitivity = Minecraft.INSTANCE.options.mouseSensitivity;

        if (PolishedClient.CONFIG.smoothCamera.get()) {
            originalSmoothCamera = Minecraft.INSTANCE.options.smoothCamera;
            Minecraft.INSTANCE.options.smoothCamera = true;

            // reset these so the previous smooth camera movement isn't applied
            Minecraft.INSTANCE.gameRenderer.smoothUtilMouseX = new SmoothUtil();
            Minecraft.INSTANCE.gameRenderer.smoothUtilMouseY = new SmoothUtil();
        }

        updateSensitivity();
    }

    public void restoreOptions() {
        Minecraft.INSTANCE.options.mouseSensitivity = originalSensitivity;
        Minecraft.INSTANCE.options.smoothCamera = originalSmoothCamera;
    }

    private void updateSensitivity() {
        if (PolishedClient.CONFIG.decreaseSensitivity.get()) {
            Minecraft.INSTANCE.options.mouseSensitivity = originalSensitivity / (divisor * divisor);
        }
    }

    public boolean scroll(double amount) {
        if (active && PolishedClient.CONFIG.zoomScrolling.get() && amount != 0) {
            setDivisor((float) Math.max(1, divisor + (amount / Math.abs(amount))));
            updateSensitivity();
            return true;
        }

        return false;
    }

    private float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public void tick() {
        lastAnimatedFactor = animatedFactor;
        animatedFactor += (targetFactor - animatedFactor) * (PolishedClient.CONFIG.zoomSpeed.get() / 10F);
    }
}
