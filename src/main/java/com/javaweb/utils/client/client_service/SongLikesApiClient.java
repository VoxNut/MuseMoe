package com.javaweb.utils.client.client_service;


public interface SongLikesApiClient {

    Boolean createNewSongLikes(Long songId);

    Boolean checkSongLiked(Long songId);
}
