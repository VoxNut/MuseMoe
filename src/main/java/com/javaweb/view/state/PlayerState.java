package com.javaweb.view.state;

import com.javaweb.model.dto.SongDTO;

import java.io.IOException;


public interface PlayerState {
    /**
     * Handle the play action
     */
    void play();

    /**
     * Handle the pause action
     */
    void pause() throws IOException;

    /**
     * Handle the stop action
     */
    void stop() throws IOException;

    /**
     * Handle transition to the next song
     */
    void next() throws IOException;

    /**
     * Handle transition to the previous song
     */
    void previous() throws IOException;

    /**
     * Handle loading a song
     */
    void loadSong(SongDTO song) throws IOException;

    /**
     * Handle the replay 5 seconds action
     */
    void replayFiveSeconds() throws IOException;

    /**
     * Handle the shuffle playlist action
     */
    void shufflePlaylist() throws IOException;

    /**
     * Handle song completion
     */
    void handlePlaybackFinished(int frame);

    /**
     * Get the name of this state
     */
    String getName();
}