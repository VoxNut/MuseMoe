package com.javaweb.client.client_service;


public interface SongLikesApiClient {

    Boolean createNewSongLikes(Long songId);

    Boolean checkSongLiked(Long songId);

    Boolean deleteSongLikes(Long songId);

}
