package com.javaweb.view.custom.musicplayer.command;

import java.util.ArrayDeque;
import java.util.Deque;

public class MusicPlayerInvoker {
    private final Deque<MusicCommand> commandHistory = new ArrayDeque<>();

    public void executeCommand(MusicCommand command) {
        command.execute();
        commandHistory.push(command);

        // Limit history size (optional)
        if (commandHistory.size() > 20) {
            commandHistory.removeLast();
        }
    }

    public void undoLastCommand() {
        if (!commandHistory.isEmpty()) {
            MusicCommand lastCommand = commandHistory.pop();
            lastCommand.undo();
        }
    }
}