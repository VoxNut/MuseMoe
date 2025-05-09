package com.javaweb.view.state;

import com.javaweb.model.dto.SongDTO;
import com.javaweb.view.MusicPlayer;

import java.io.IOException;

public class StoppedState implements PlayerState {

    private final MusicPlayer player;

    public StoppedState(MusicPlayer player) {
        this.player = player;
    }

    @Override
    public void play() {

    }

    @Override
    public void pause() throws IOException {

    }

    @Override
    public void stop() throws IOException {

    }

    @Override
    public void next() throws IOException {

    }

    @Override
    public void previous() throws IOException {

    }

    @Override
    public void loadSong(SongDTO song) throws IOException {

    }

    @Override
    public void replayFiveSeconds() throws IOException {

    }

    @Override
    public void shufflePlaylist() throws IOException {

    }

    @Override
    public void handlePlaybackFinished(int frame) {

    }

    @Override
    public String getName() {
        return "";
    }
}
