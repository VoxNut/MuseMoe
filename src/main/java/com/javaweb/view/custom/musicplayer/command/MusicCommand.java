package com.javaweb.view.custom.musicplayer.command;

public interface MusicCommand {

    void execute();

    default void undo() {

    }
}
