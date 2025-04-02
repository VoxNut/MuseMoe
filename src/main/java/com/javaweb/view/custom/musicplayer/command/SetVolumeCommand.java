package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.view.custom.musicplayer.MusicPlayer;

public class SetVolumeCommand implements MusicCommand {
    private final MusicPlayer musicPlayer;
    private final int value;

    public SetVolumeCommand(MusicPlayer musicPlayer, int value) {
        this.musicPlayer = musicPlayer;
        this.value = value;
    }

    @Override
    public void execute() {
        musicPlayer.setVolume(value);
    }
}
