package io.github.kimovoid.polished.mixin.client.debug;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.PrintWriter;

@Mixin(GameOptions.class)
public class GameOptionsMixin {

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/lang/String;split(Ljava/lang/String;)[Ljava/lang/String;"))
    public void loadOptions(CallbackInfo ci, @Local String string) {
        String[] stringArray = string.split(":");
        PolishedClient.INSTANCE.debugOptions.load(stringArray);
    }

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Ljava/io/PrintWriter;close()V"))
    private void saveOptions(CallbackInfo ci, @Local PrintWriter printWriter) {
        PolishedClient.INSTANCE.debugOptions.save(printWriter);
    }
}
