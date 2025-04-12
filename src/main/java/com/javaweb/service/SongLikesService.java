package com.javaweb.service;

public interface SongLikesService {


    boolean createSongLikes(Long songId);

    boolean checkSongLiked(Long songId);

    boolean deleteSongLikes(Long songId);

}
