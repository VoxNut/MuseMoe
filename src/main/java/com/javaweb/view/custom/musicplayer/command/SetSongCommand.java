package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.view.custom.musicplayer.MusicPlayer;

public class SetSongCommand implements MusicCommand {
    private final MusicPlayer musicPlayer;
    private final SongDTO songDTO;

    public SetSongCommand(MusicPlayer musicPlayer, SongDTO songDTO) {
        this.musicPlayer = musicPlayer;
        this.songDTO = songDTO;
    }

    @Override
    public void execute() {
        musicPlayer.setCurrentSong(songDTO);
    }
}
