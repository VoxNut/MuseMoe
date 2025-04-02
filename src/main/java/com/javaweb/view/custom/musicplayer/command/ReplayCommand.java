package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.view.custom.musicplayer.MusicPlayer;

import java.io.IOException;

public class ReplayCommand implements MusicCommand {
    private final MusicPlayer player;

    public ReplayCommand(MusicPlayer player) {
        this.player = player;
    }

    @Override
    public void execute() {
        try {
            player.replayFiveSeconds();
        } catch (IOException e) {
            throw new RuntimeException("Error replaying", e);
        }
    }
}