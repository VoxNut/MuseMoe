package com.javaweb.service;

import com.javaweb.model.dto.SongLikesDTO;

import java.util.List;

public interface SongLikesService {


    boolean createSongLikes(Long songId);

    boolean checkSongLiked(Long songId);

    boolean deleteSongLikes(Long songId);

    List<SongLikesDTO> findAllByUser();
}
