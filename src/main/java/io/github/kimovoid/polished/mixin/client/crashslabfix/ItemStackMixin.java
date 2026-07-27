package io.github.kimovoid.polished.mixin.client.crashslabfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.StoneSlabBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SlabItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public class ItemStackMixin {

    @WrapOperation(
            method = "getTranslationKey",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/Item;getTranslationKey(Lnet/minecraft/item/ItemStack;)Ljava/lang/String;"
            )
    )
    public String fixCrashSlab(Item instance, ItemStack stack, Operation<String> original) {
        if (instance instanceof SlabItem && stack.getMetadata() > 3) {
            return "tile.doubleSlab." + StoneSlabBlock.VARIANTS[stack.getMetadata() - 4];
        }
        return original.call(instance, stack);
    }
}
