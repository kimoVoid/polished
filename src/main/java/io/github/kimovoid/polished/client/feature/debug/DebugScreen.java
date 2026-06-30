package io.github.kimovoid.polished.client.feature.debug;

import io.github.kimovoid.polished.client.event.RenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.locale.Language;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.HitResult;
import net.minecraft.world.LightType;
import net.ornithemc.osl.branding.api.BrandingContext;
import net.ornithemc.osl.branding.impl.BrandingPatchImpl;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DebugScreen {

    public static final DebugScreen INSTANCE = new DebugScreen();
    private String cpuInfo;

    public void init() {
        try {
            CentralProcessor cpu = new SystemInfo().getHardware().getProcessor();
            this.cpuInfo = String.format("%dx %s", cpu.getLogicalProcessorCount(), cpu.getProcessorIdentifier().getName()).replaceAll("\\s+", " ");
        } catch (Throwable ignored) {
        }
    }

    public void renderLeft(Minecraft mc) {
        int x = MathHelper.floor(mc.player.x);
        int y = MathHelper.floor(mc.player.y);
        int z = MathHelper.floor(mc.player.z);
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        long time = MathHelper.floor((double) (mc.world.getTime() / 24000L));
        String biome = mc.world.getBiomeSource().getBiome(x, z).name;
        float angle = (mc.world.getTimeOfDay(1.0F) * 24.0F + 12.0F) % 24.0F;
        int h = (int) Math.floor(angle);
        int m = (int) Math.floor(angle * 60.0F) - h * 60;
        int s = (int) Math.floor(angle * 3600.0F) - h * 3600 - m * 60;
        int f = (MathHelper.floor((double) (mc.player.yaw * 4.0f / 360.0f) + 0.5) & 3);
        String facing = f == 0 ? "west (Towards positive Z)"
                : f == 1 ? "north (Towards negative X)"
                  : f == 2 ? "east (Towards negative Z)"
                    : "south (Towards positive X)";

        /* Add lines */
        List<String> lines = new ArrayList<>();
        String version = getVersion();
        lines.add(String.format("Minecraft %s (%s%s)", getVersionPrefixed(), version, BrandingPatchImpl.apply(BrandingContext.TITLE_SCREEN, "")));
        lines.add(mc.fpsDebugInfo);
        lines.add(mc.getRenderChunkDebugInfo());
        lines.add(mc.getRenderEntityDebugInfo());
        lines.add(mc.getWorldDebugInfo());
        lines.add(mc.getChunkSourceDebugInfo());
        lines.add("");
        lines.add(String.format("XYZ: %.3f / %.3f / %.3f", mc.player.x, mc.player.y, mc.player.z));
        lines.add(String.format("Block: %s %s %s", x, y, z));
        lines.add(String.format("Facing: %s (%.1f / %.1f)", facing, this.wrapDegrees(mc.player.yaw), this.wrapDegrees(mc.player.pitch)));
        lines.add("Chunk: " + String.format("%s, %s [%s, %s]", chunkX, chunkZ, x & 15, z & 15));
        lines.add("");
        lines.add(String.format("Light: %s (bl: %s, sky: %s)", mc.world.getActualLight(x, y, z), mc.world.getLight(LightType.BLOCK, x, y, z), mc.world.getLight(LightType.SKY, x, y, z)));
        lines.add("Biome: " + (biome == null ? "Unknown" : biome));
        lines.add("Day: " + time);
        lines.add("Time: " + String.format("%02d:%02d:%02d", h, m, s));
        lines.add("Slime: " + this.isSlimeChunk(mc.world.getSeed(), mc.player.chunkX, mc.player.chunkZ));

        /* Call event so mods can add/remove lines */
        RenderEvents.DebugRenderEvent event = new RenderEvents.DebugRenderEvent(RenderEvents.DebugSide.LEFT, lines);
        RenderEvents.RENDER_DEBUG.invoker().accept(event);

        /* Render lines */
        for (int i = 0; i < event.getLines().size(); i++) {
            this.renderText(event.getLines().get(i), 2, 2 + i * 9);
        }
    }

    public void renderRight(Minecraft mc) {
        Window window = new Window(mc.options, mc.width, mc.height);
        int width = window.getWidth();
        TextRenderer tr = mc.textRenderer;

        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;

        /* Add lines */
        List<String> lines = new ArrayList<>();
        lines.add(String.format("Java: %s %sbit", System.getProperty("java.version"), System.getProperty("sun.arch.data.model")));
        lines.add("Mem: " + usedMem * 100L / maxMem + "% (" + usedMem / 1024L / 1024L + "MB) of " + maxMem / 1024L / 1024L + "MB");
        lines.add("Allocated: " + totalMem * 100L / maxMem + "% (" + totalMem / 1024L / 1024L + "MB)");
        lines.add("LWJGL " + Sys.getVersion());
        lines.add("");

        if (this.cpuInfo != null) {
            lines.add(String.format("CPU: %s", this.cpuInfo));
            lines.add("");
        }
        lines.add(String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), GL11.glGetString(7936)));
        lines.add(GL11.glGetString(7937));
        lines.add(GL11.glGetString(7938));
        if (mc.world != null && mc.crosshairTarget != null && mc.crosshairTarget.type == HitResult.Type.BLOCK) {
            int id = mc.world.getBlock(mc.crosshairTarget.x, mc.crosshairTarget.y, mc.crosshairTarget.z);
            int meta = mc.world.getBlockMetadata(mc.crosshairTarget.x, mc.crosshairTarget.y, mc.crosshairTarget.z);
            ItemStack stack = new ItemStack(id, 1, meta);
            String name = Language.getInstance().translateName(stack.getTranslationKey()).trim();

            lines.add("");
            lines.add(String.format("Looking at: %s, %s, %s", mc.crosshairTarget.x, mc.crosshairTarget.y, mc.crosshairTarget.z));
            lines.add(String.format("%s (#%s%s)", name.isEmpty() ? stack.getTranslationKey() : name, stack.id, stack.getMetadata() != 0 && !stack.isDamaged() ? "/" + stack.getMetadata() : ""));
        }

        /* Call event so mods can add/remove lines */
        RenderEvents.DebugRenderEvent event = new RenderEvents.DebugRenderEvent(RenderEvents.DebugSide.RIGHT, lines);
        RenderEvents.RENDER_DEBUG.invoker().accept(event);

        /* Render lines */
        for (int i = 0; i < event.getLines().size(); i++) {
            String line = event.getLines().get(i);
            this.renderText(line, width - tr.getWidth(line) - 1, 2 + i * 9);
        }
    }

    private boolean isSlimeChunk(long seed, int x, int z) {
        Random rnd = new Random(
                seed +
                        (int) (x * x * 0x4c1906) +
                        (int) (x * 0x5ac0db) +
                        (int) (z * z) * 0x4307a7L +
                        (int) (z * 0x5f24f) ^ 0x3ad8025fL
        );

        return rnd.nextInt(10) == 0;
    }

    private void renderText(String text, int x, int y) {
        if (text.isEmpty()) {
            return;
        }
        Minecraft.INSTANCE.gui.fillGradient(x - 1, y - 1, x + Minecraft.INSTANCE.textRenderer.getWidth(text), y + 8, -1873784752, -1873784752);
        Minecraft.INSTANCE.textRenderer.draw(text, x, y, 0xE0E0E0);
    }

    private float wrapDegrees(float degrees) {
        if ((degrees %= 360.0f) >= 180.0f) {
            degrees -= 360.0f;
        }
        if (degrees < -180.0f) {
            degrees += 360.0f;
        }
        return degrees;
    }

    private String getVersion() {
        String version = FabricLoader.getInstance()
                .getModContainer("minecraft")
                .get()
                .getMetadata()
                .getVersion()
                .getFriendlyString();

        if (version.startsWith("1.0.0-alpha.")) {
            return "a1." + version.substring(12);
        } else if (version.startsWith("1.0.0-beta.")) {
            return "b1." + version.substring(11);
        } else if (version.equals("1.0.0")) {
            return "1.0";
        }

        return version;
    }

    private String getVersionPrefixed() {
        String version = getVersion();
        return version.startsWith("b") ? "Beta " + version.substring(1) : version.startsWith("a") ? "Alpha " + version.substring(1) : version;
    }
}
