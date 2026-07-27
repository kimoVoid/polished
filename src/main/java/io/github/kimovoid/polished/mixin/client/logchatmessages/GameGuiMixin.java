package io.github.kimovoid.polished.mixin.client.logchatmessages;

import io.github.kimovoid.polished.client.PolishedClient;
import net.minecraft.client.gui.GameGui;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(GameGui.class)
public class GameGuiMixin {

    @Unique private final static Logger CHAT_LOGGER = LogManager.getLogger("CHAT");
    @Unique private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

    @Inject(method = "addChatMessage", at = @At(value = "INVOKE", target = "Ljava/util/List;add(ILjava/lang/Object;)V"))
    private void log(String text, CallbackInfo ci) {
        if (PolishedClient.CONFIG.logChatMessages.get()) {
            String message = STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
            CHAT_LOGGER.info(message);
        }
    }
}
