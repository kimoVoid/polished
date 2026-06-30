package io.github.kimovoid.polished;

import io.github.kimovoid.polished.command.CommandRegistry;
import io.github.kimovoid.polished.command.PingCommand;
import net.fabricmc.loader.api.FabricLoader;
import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import net.ornithemc.osl.networking.api.ChannelIdentifiers;
import net.ornithemc.osl.networking.api.ChannelRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Polished implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("Polished");
    public static final NamespacedIdentifier PLAYER_INFO_CHANNEL =
            ChannelRegistry.register(ChannelIdentifiers.from("polished", "playerinfo"), true, false);

    @Override
    public void init() {
        /* Retro Commands */
        if (FabricLoader.getInstance().isModLoaded("retrocommands")) {
            CommandRegistry.register(new PingCommand());
        }
    }
}
