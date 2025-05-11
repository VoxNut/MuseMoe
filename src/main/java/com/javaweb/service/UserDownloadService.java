package com.javaweb.service;

import com.javaweb.model.dto.SongDTO;

import java.util.List;

public interface UserDownloadService {

    List<SongDTO> findAllDownloadedSongs();


    boolean createNewUserDownload(Long songId);
}
