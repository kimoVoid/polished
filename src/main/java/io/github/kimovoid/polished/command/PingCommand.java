package io.github.kimovoid.polished.command;

import com.periut.retrocommands.api.Command;
import com.periut.retrocommands.util.SharedCommandSource;
import io.github.kimovoid.polished.server.PolishedServer;
import io.github.kimovoid.polished.server.feature.ping.PlayerPing;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;

public class PingCommand implements Command {

    @Override
    public void command(SharedCommandSource source, String[] args) {
        ServerPlayerEntity target = ((ServerPlayerEntity) source.getPlayer());
        if (args.length > 1) {
            target = PolishedServer.SERVER.playerManager.get(args[1]);
        }

        if (target == null) {
            source.sendFeedback("Invalid player \"" + args[1] + "\"");
            return;
        }

        source.sendFeedback(String.format("%s ping: %s ms", target.name.equals(source.getName()) ? "Your" : target.name + "'s", ((PlayerPing)target).getPing()));
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public void manual(SharedCommandSource commandSource) {
        commandSource.sendFeedback("Usage: /ping [player]");
        commandSource.sendFeedback("Info: Check a player's ping");
    }

    @Override
    public boolean disableInSingleplayer() {
        return true;
    }

    @Override
    public boolean needsPermissions() {
        return false;
    }
}
