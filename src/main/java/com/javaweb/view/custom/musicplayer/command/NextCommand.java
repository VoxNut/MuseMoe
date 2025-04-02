package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.view.custom.musicplayer.MusicPlayer;

import java.io.IOException;

public class NextCommand implements MusicCommand {
    private final MusicPlayer player;

    public NextCommand(MusicPlayer player) {
        this.player = player;
    }

    @Override
    public void execute() {
        try {
            player.nextSong();
        } catch (IOException e) {
            throw new RuntimeException("Error playing next song", e);
        }
    }
}