package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.view.custom.musicplayer.MusicPlayer;

import java.io.IOException;

public class PauseCommand implements MusicCommand {
    private final MusicPlayer player;

    public PauseCommand(MusicPlayer player) {
        this.player = player;
    }

    @Override
    public void execute() {
        try {
            player.pauseSong();
        } catch (IOException e) {
            throw new RuntimeException("Error pausing song", e);
        }
    }
}