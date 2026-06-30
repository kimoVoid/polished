package io.github.kimovoid.polished.mixin.client.debug;

import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.mob.player.ClientPlayerEntity;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.client.render.world.WorldRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderLastChunks", at = @At("TAIL"))
    private void renderChunkBorders(int layer, double tickDelta, CallbackInfo ci) {
        if (!PolishedClient.INSTANCE.debugOptions.renderChunkBorders) {
            return;
        }

        ClientPlayerEntity player = Minecraft.INSTANCE.player;
        double dX = player.prevX + (player.x - player.prevX) * tickDelta;
        double dY = player.prevY + (player.y - player.prevY) * tickDelta;
        double dZ = player.prevZ + (player.z - player.prevZ) * tickDelta;
        double bot = -dY;
        double top = 256D - dY;
        double cX = player.chunkX * 16 - dX;
        double cZ = player.chunkZ * 16 - dZ;

        Tesselator tessellator = Tesselator.INSTANCE;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        //GL11.glDisable(GL11.GL_BLEND);
        GL11.glLineWidth(1F);
        tessellator.begin(GL11.GL_CURRENT_BIT | GL11.GL_POINT_BIT);

        for (int x = -16; x <= 32; x += 16) {
            for (int z = -16; z <= 32; z += 16) {
                tessellator.color(255, 0, 0, 0);
                tessellator.vertex(cX + x, bot, cZ + z);

                tessellator.color(255, 0, 0, 127);
                tessellator.vertex(cX + x, bot, cZ + z);
                tessellator.vertex(cX + x, top, cZ + z);

                tessellator.color(255, 0, 0, 0);
                tessellator.vertex(cX + x, top, cZ + z);
            }
        }

        for (int x = 2; x < 16; x += 2) {
            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX + x, bot, cZ);

            tessellator.color(255, 255, 0);
            tessellator.vertex(cX + x, bot, cZ);
            tessellator.vertex(cX + x, top, cZ);

            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX + x, top, cZ);
            tessellator.vertex(cX + x, bot, cZ + 16D);

            tessellator.color(255, 255, 0);
            tessellator.vertex(cX + x, bot, cZ + 16D);
            tessellator.vertex(cX + x, top, cZ + 16D);

            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX + x, top, cZ + 16D);
        }

        for (int z = 2; z < 16; z += 2) {
            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX, bot, cZ + z);

            tessellator.color(255, 255, 0);
            tessellator.vertex(cX, bot, cZ + z);
            tessellator.vertex(cX, top, cZ + z);

            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX, top, cZ + z);
            tessellator.vertex(cX + 16D, bot, cZ + z);

            tessellator.color(255, 255, 0);
            tessellator.vertex(cX + 16D, bot, cZ + z);
            tessellator.vertex(cX + 16D, top, cZ + z);

            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX + 16D, top, cZ + z);
        }

        for (int y = 0; y <= 256; y += 2) {
            double yLine = y - dY;
            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX, yLine, cZ);

            tessellator.color(255, 255, 0);
            tessellator.vertex(cX, yLine, cZ);
            tessellator.vertex(cX, yLine, cZ + 16D);
            tessellator.vertex(cX + 16D, yLine, cZ + 16D);
            tessellator.vertex(cX + 16D, yLine, cZ);
            tessellator.vertex(cX, yLine, cZ);

            tessellator.color(255, 255, 0, 0);
            tessellator.vertex(cX, yLine, cZ);
        }

        tessellator.end();
        GL11.glLineWidth(2F);
        tessellator.begin(GL11.GL_CURRENT_BIT | GL11.GL_POINT_BIT);

        for (int x = 0; x <= 16; x += 16) {
            for (int z = 0; z <= 16; z += 16) {
                tessellator.color(63, 63, 255, 0);
                tessellator.vertex(cX + x, bot, cZ + z);

                tessellator.color(63, 63, 255);
                tessellator.vertex(cX + x, bot, cZ + z);
                tessellator.vertex(cX + x, top, cZ + z);

                tessellator.color(63, 63, 255, 0);
                tessellator.vertex(cX + x, top, cZ + z);
            }
        }

        for (int y = 0; y <= 256; y += 16) {
            double yLine = y - dY;
            tessellator.color(63, 63, 255, 0);
            tessellator.vertex(cX, yLine, cZ);

            tessellator.color(63, 63, 255);
            tessellator.vertex(cX, yLine, cZ);
            tessellator.vertex(cX, yLine, cZ + 16D);
            tessellator.vertex(cX + 16D, yLine, cZ + 16D);
            tessellator.vertex(cX + 16D, yLine, cZ);
            tessellator.vertex(cX, yLine, cZ);

            tessellator.color(63, 63, 255, 0);
            tessellator.vertex(cX, yLine, cZ);
        }

        tessellator.end();
        GL11.glLineWidth(1F);
        //GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
