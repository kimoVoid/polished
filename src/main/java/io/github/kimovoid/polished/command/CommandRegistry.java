package io.github.kimovoid.polished.command;

import com.periut.retrocommands.api.Command;

public class CommandRegistry {

    public static void register(Command cmd) {
        com.periut.retrocommands.api.CommandRegistry.add(cmd);
    }
}
