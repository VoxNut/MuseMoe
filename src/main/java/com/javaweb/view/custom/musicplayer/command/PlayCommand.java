package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.view.custom.musicplayer.MusicPlayer;

public class PlayCommand implements MusicCommand {
    private final MusicPlayer player;

    public PlayCommand(MusicPlayer player) {
        this.player = player;
    }

    @Override
    public void execute() {
        player.playCurrentSong();
    }
}