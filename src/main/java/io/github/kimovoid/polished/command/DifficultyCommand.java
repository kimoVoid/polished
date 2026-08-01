package io.github.kimovoid.polished.command;

import com.periut.retrocommands.api.Command;
import com.periut.retrocommands.util.ServerUtil;
import com.periut.retrocommands.util.SharedCommandSource;
import io.github.kimovoid.polished.server.PolishedServer;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;

public class DifficultyCommand implements Command {

    @Override
    public void command(SharedCommandSource commandSource, String[] parameters) {
        if (parameters.length < 2) {
            int difficulty = PolishedServer.INSTANCE.properties.difficulty;
            String current = difficulty == 0 ? "peaceful" : difficulty == 1 ? "easy" : difficulty == 2 ? "normal" : "hard";
            commandSource.sendFeedback("Current difficulty: " + current);
            return;
        }

        String arg = parameters[1];
        int difficulty;
        switch (arg.toLowerCase()) {
            case "peaceful" -> difficulty = 0;
            case "easy" -> difficulty = 1;
            case "normal" -> difficulty = 2;
            case "hard" -> difficulty = 3;
            default -> {
                this.manual(commandSource);
                return;
            }
        }

        PolishedServer.INSTANCE.properties.difficulty = difficulty;
        for (ServerWorld world : ServerUtil.getServer().worlds) {
            world.difficulty = difficulty;
        }
        ServerUtil.sendFeedbackAndLog(commandSource.getName(), "Set server difficulty to " + arg.toLowerCase());
    }

    @Override
    public String name() {
        return "difficulty";
    }

    @Override
    public void manual(SharedCommandSource commandSource) {
        commandSource.sendFeedback("Usage: /difficulty (difficulty)");
        commandSource.sendFeedback("Sets the server difficulty");
        commandSource.sendFeedback("Values: peaceful, easy, normal, hard");
    }

    @Override
    public boolean disableInSingleplayer() {
        return true;
    }

    @Override
    public String[] suggestion(SharedCommandSource source, int parameterNum, String currentInput, String totalInput) {
        if (parameterNum == 1) {
            String[] options = {"peaceful", "easy", "normal", "hard"};
            ArrayList<String> output = new ArrayList<>();
            for (String option : options) {
                if (option.startsWith(currentInput)) {
                    output.add(option.substring(currentInput.length()));
                }
            }
            return output.toArray(new String[0]);
        }
        return new String[0];
    }
}
