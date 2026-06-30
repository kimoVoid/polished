package io.github.kimovoid.polished.mixin.client.debug;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.menu.InventoryMenuScreen;
import net.minecraft.inventory.slot.InventorySlot;
import net.minecraft.item.ItemStack;
import net.minecraft.locale.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryMenuScreen.class)
public abstract class InventoryMenuScreenMixin extends Screen {

    @Shadow protected int backgroundWidth;
    @Shadow protected int backgroundHeight;
    @Shadow protected abstract InventorySlot getSlot(int x, int y);

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Ljava/lang/String;length()I"))
    private int replaceTooltip(String instance, Operation<Integer> original, int mouseX, int mouseY) {
        if (PolishedClient.INSTANCE.debugOptions.showAdvancedTooltips && !instance.isEmpty()) {
            InventorySlot slot = getSlot(mouseX, mouseY);
            if (slot == null || slot.getItem() == null) {
                return 0; // not gonna happen but ide cries
            }

            ItemStack stack = slot.getItem();
            String name = Language.getInstance().translateName(stack.getTranslationKey()).trim()
                    + String.format(" (#%s%s)", stack.id, stack.getMetadata() != 0 && !stack.isDamaged() ? "/" + stack.getMetadata() : "");
            String dur = !stack.isDamaged() ? "" : String.format("Durability: %s/%s", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage());

            int x = mouseX - (this.width - this.backgroundWidth) / 2 + 12;
            int y = mouseY - (this.height - this.backgroundHeight) / 2 - 12;
            int w = Math.max(this.textRenderer.getWidth(name), this.textRenderer.getWidth(dur));
            int h = stack.isDamaged() ? 20 : 8;

            this.fillGradient(x - 3, y - 3, x + 3 + w, y + 3 + h, -1073741824, -1073741824);
            this.textRenderer.drawWithShadow(name, x, y, -1);
            if (stack.isDamaged()) {
                this.textRenderer.drawWithShadow(dur, x, y + 12, 11842740);
            }
            return 0;
        }
        return original.call(instance);
    }
}
