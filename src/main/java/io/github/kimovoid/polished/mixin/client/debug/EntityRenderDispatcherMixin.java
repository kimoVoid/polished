package io.github.kimovoid.polished.mixin.client.debug;

import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.platform.Lighting;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;DDDFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderer;postRender(Lnet/minecraft/entity/Entity;DDDFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderHitboxes(Entity entity, double dx, double dy, double dz, float yaw, float tickDelta, CallbackInfo ci) {
        if (!PolishedClient.INSTANCE.debugOptions.renderHitboxes || entity.shape == null) {
            return;
        }

        this.renderHitbox(entity, dx, dy, dz, yaw, tickDelta);
    }

    @Unique
    private void renderHitbox(Entity entity, double dx, double dy, double dz, float yaw, float tickDelta) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Lighting.turnOff();
        //GL11.glDisable(GL11.GL_BLEND);

        float f = entity.width / 2.0F;
        Box box = entity.shape;
        Box box2 = Box.of(
                box.minX - entity.x + dx,
                box.minY - entity.y + dy,
                box.minZ - entity.z + dz,
                box.maxX - entity.x + dx,
                box.maxY - entity.y + dy,
                box.maxZ - entity.z + dz
        );

        this.renderOutlineShape(box2, 255, 255, 255, 255);
        if (entity instanceof MobEntity) {
            float g = 0.01F;
            this.renderOutlineShape(
                    Box.of(dx - f, dy + entity.getEyeHeight() - 0.01F, dz - f, dx + f, dy + entity.getEyeHeight() + 0.01F, dz + f), 255, 0, 0, 255
            );

            Tesselator tesselator = Tesselator.INSTANCE;
            Vec3d vec3d = ((MobEntity)entity).getLookVector(tickDelta);
            tesselator.begin(GL11.GL_LINE_STRIP);
            tesselator.color(0, 0, 255, 255);
            tesselator.vertex(dx, dy + entity.getEyeHeight(), dz);
            tesselator.vertex(dx + vec3d.x * 2.0, dy + entity.getEyeHeight() + vec3d.y * 2.0, dz + vec3d.z * 2.0);
            tesselator.end();
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Lighting.turnOn();
        //GL11.glEnable(GL11.GL_BLEND);
    }

    @Unique
    private void renderOutlineShape(Box shape, int r, int g, int b, int a) {
        Tesselator tesselator = Tesselator.INSTANCE;
        tesselator.begin(GL11.GL_LINE_STRIP);
        tesselator.color(r, g, b, a);
        tesselator.vertex(shape.minX, shape.minY, shape.minZ);
        tesselator.vertex(shape.maxX, shape.minY, shape.minZ);
        tesselator.vertex(shape.maxX, shape.minY, shape.maxZ);
        tesselator.vertex(shape.minX, shape.minY, shape.maxZ);
        tesselator.vertex(shape.minX, shape.minY, shape.minZ);
        tesselator.end();

        tesselator.begin(GL11.GL_LINE_STRIP);
        tesselator.color(r, g, b, a);
        tesselator.vertex(shape.minX, shape.maxY, shape.minZ);
        tesselator.vertex(shape.maxX, shape.maxY, shape.minZ);
        tesselator.vertex(shape.maxX, shape.maxY, shape.maxZ);
        tesselator.vertex(shape.minX, shape.maxY, shape.maxZ);
        tesselator.vertex(shape.minX, shape.maxY, shape.minZ);
        tesselator.end();
        
        tesselator.begin(GL11.GL_CURRENT_BIT);
        tesselator.color(r, g, b, a);
        tesselator.vertex(shape.minX, shape.minY, shape.minZ);
        tesselator.vertex(shape.minX, shape.maxY, shape.minZ);
        tesselator.vertex(shape.maxX, shape.minY, shape.minZ);
        tesselator.vertex(shape.maxX, shape.maxY, shape.minZ);
        tesselator.vertex(shape.maxX, shape.minY, shape.maxZ);
        tesselator.vertex(shape.maxX, shape.maxY, shape.maxZ);
        tesselator.vertex(shape.minX, shape.minY, shape.maxZ);
        tesselator.vertex(shape.minX, shape.maxY, shape.maxZ);
        tesselator.end();
    }
}
