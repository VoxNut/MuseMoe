package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.view.custom.musicplayer.MusicPlayer;

public class SetPlaylistCommand implements MusicCommand {
    private final MusicPlayer musicPlayer;
    private final PlaylistDTO playlistDTO;


    public SetPlaylistCommand(MusicPlayer musicPlayer, PlaylistDTO playlistDTO) {
        this.musicPlayer = musicPlayer;
        this.playlistDTO = playlistDTO;
    }

    @Override
    public void execute() {
        musicPlayer.setCurrentPlaylist(playlistDTO);
    }
}
