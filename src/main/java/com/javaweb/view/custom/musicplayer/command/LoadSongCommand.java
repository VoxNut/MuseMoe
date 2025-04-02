package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.view.custom.musicplayer.MusicPlayer;

public class LoadSongCommand implements MusicCommand {

    private final MusicPlayer musicPlayer;
    private final SongDTO songDTO;

    public LoadSongCommand(MusicPlayer musicPlayer, SongDTO songDTO) {
        this.musicPlayer = musicPlayer;
        this.songDTO = songDTO;
    }

    @Override
    public void execute() {
        try {
            musicPlayer.loadSong(songDTO);
        } catch (Exception e) {
            throw new RuntimeException("Error loading song", e);
        }
    }
}
