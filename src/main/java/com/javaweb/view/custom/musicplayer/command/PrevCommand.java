package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.view.custom.musicplayer.MusicPlayer;

import java.io.IOException;

public class PrevCommand implements MusicCommand {
    private final MusicPlayer player;

    public PrevCommand(MusicPlayer player) {
        this.player = player;
    }

    @Override
    public void execute() {
        try {
            player.prevSong();
        } catch (IOException e) {
            throw new RuntimeException("Error playing previous song", e);
        }
    }
}