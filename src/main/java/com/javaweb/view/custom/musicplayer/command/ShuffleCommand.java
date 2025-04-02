package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.view.custom.musicplayer.MusicPlayer;

import java.io.IOException;

public class ShuffleCommand implements MusicCommand {
    private final MusicPlayer player;

    public ShuffleCommand(MusicPlayer player) {
        this.player = player;
    }

    @Override
    public void execute() {
        try {
            player.shufflePlaylist();
        } catch (IOException e) {
            throw new RuntimeException("Error shuffling playlist", e);
        }
    }
}