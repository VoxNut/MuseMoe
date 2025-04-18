package com.javaweb.client.client_service;


import com.javaweb.model.dto.SongLikesDTO;

import java.util.List;

public interface SongLikesApiClient {

    Boolean createNewSongLikes(Long songId);

    Boolean checkSongLiked(Long songId);

    Boolean deleteSongLikes(Long songId);

    List<SongLikesDTO> findAllSongLikesByUser();

}
