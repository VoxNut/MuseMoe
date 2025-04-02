package com.javaweb.view.custom.musicplayer.command;

import com.javaweb.model.dto.PlaylistDTO;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.view.custom.musicplayer.MusicPlayer;

public class MusicCommandFactory {
    private final MusicPlayer player;
   
    public MusicCommandFactory(MusicPlayer player) {
        this.player = player;
    }

    public MusicCommand createPlayCommand() {
        return new PlayCommand(player);
    }

    public MusicCommand createPauseCommand() {
        return new PauseCommand(player);
    }

    public MusicCommand createNextCommand() {
        return new NextCommand(player);
    }

    public MusicCommand createPrevCommand() {
        return new PrevCommand(player);
    }

    public MusicCommand createShuffleCommand() {
        return new ShuffleCommand(player);
    }

    public MusicCommand createReplayCommand() {
        return new ReplayCommand(player);
    }

    public MusicCommand createLoadSongCommand(SongDTO songDTO) {
        return new LoadSongCommand(player, songDTO);
    }

    public MusicCommand createSetPlaylistCommand(PlaylistDTO playlistDTO) {
        return new SetPlaylistCommand(player, playlistDTO);
    }

    public MusicCommand createSetVolumeCommand(int value) {
        return new SetVolumeCommand(player, value);
    }

    public MusicCommand createSetSongCommand(SongDTO songDTO) {
        return new SetSongCommand(player, songDTO);
    }
}