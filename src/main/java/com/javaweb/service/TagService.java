package com.javaweb.service;

import com.javaweb.entity.SongEntity;
import com.javaweb.entity.TagEntity;
import com.javaweb.model.dto.TagDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TagService {
    Set<TagEntity> generateTagsForSong(SongEntity song);

    void autoTagSongs(List<Long> songIds);

    List<TagDTO> findTagsBySongId(Long songId);

    Map<String, Integer> fetchTopTags(int limit);

    List<TagDTO> findAllTags();
}
