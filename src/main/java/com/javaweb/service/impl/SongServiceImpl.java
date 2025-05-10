package com.javaweb.service.impl;


import com.ibm.icu.text.Transliterator;
import com.javaweb.converter.SongConverter;
import com.javaweb.entity.SongEntity;
import com.javaweb.exception.EntityNotFoundException;
import com.javaweb.model.dto.SongDTO;
import com.javaweb.model.request.SongRequestDTO;
import com.javaweb.repository.SongRepository;
import com.javaweb.service.SongService;
import com.javaweb.service.TagService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SongServiceImpl implements SongService {


    private final SongRepository songRepository;

    private final SongConverter songConverter;

    private final Transliterator latinToKana = Transliterator.getInstance("Latin-Hiragana");
    private final Transliterator kanaToLatin = Transliterator.getInstance("Hiragana-Latin");

    @PersistenceContext
    private EntityManager entityManager;


    private final TagService tagService;

    private final GoogleDriveService googleDriveService;

    @Override
    public SongDTO findOneByTitle(String title) {
        SongDTO song = songConverter.toDTO(
                songRepository.findOneByTitle(title)
                        .orElseThrow(() -> new EntityNotFoundException("Song not found!"))
        );
        return song;
    }

    @Override
    public List<SongDTO> findAllSongsLike(String title) {
        try {
            List<SongDTO> songDTOS = songRepository.findAllSongsLike(title)
                    .stream()
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
            return songDTOS;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public SongDTO findById(Long id) {
        return songConverter.toDTO(songRepository
                .findById(id)
                .orElseThrow(() -> {
                            log.warn("Song with id: {} not found", id);
                            return new RuntimeException("Failed to find song");
                        }
                ));
    }

    @Override
    public SongDTO findSongByUrl(String songUrl) {
        return songRepository.findByStreamingMediaWebContentLink(songUrl)
                .map(songConverter::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Song with URL " + songUrl + " not found"));
    }

    @Override
    public List<SongDTO> findAllSongs() {
        try {
            return songRepository.findAll()
                    .stream()
                    .map(songConverter::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional
    public Map<String, Object> createMultipleSongs(SongRequestDTO songRequestDTO) {
        List<String> successfulUploads = new ArrayList<>();
        List<String> failedUploads = new ArrayList<>();

        if (songRequestDTO.getMp3Files() == null || songRequestDTO.getMp3Files().isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "No files provided for upload"
            );
        }

        try {
            List<Map<String, String>> uploadResults = googleDriveService.uploadMultipleSongFiles(songRequestDTO.getMp3Files());

            for (Map<String, String> uploadResult : uploadResults) {
                String fileName = uploadResult.get("fileName");
                String fileId = uploadResult.get("fileId");
                if (uploadResult.containsKey("error")) {
                    failedUploads.add(fileName + " (Error: " + uploadResult.get("error") + ")");
                    continue;
                }
                songRequestDTO.setGoogleDriveFileId(fileId);
                SongEntity song = songConverter.toEntity(songRequestDTO);
                song.setTags(tagService.generateTagsForSong(song));
                songRepository.save(song);
                successfulUploads.add(fileName);
            }
        } catch (Exception e) {
            log.error("Error in batch upload process: {}", e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "message", "Error processing batch upload: " + e.getMessage()
            );
        }

        return Map.of(
                "success", !successfulUploads.isEmpty(),
                "totalFiles", songRequestDTO.getMp3Files().size(),
                "successful", successfulUploads,
                "failed", failedUploads,
                "successCount", successfulUploads.size(),
                "failureCount", failedUploads.size()
        );
    }

    @Override
    public SongDTO findByGoogleDriveId(String driveId) {
        try {
            SongEntity entity = songRepository.findByStreamingMediaGoogleDriveId(driveId);
            SongDTO res = songConverter.toDTO(entity);
            log.info("Successfully find song with {} drive Id", driveId);
            return res;
        } catch (Exception e) {
            log.error("Failed find song with {} drive Id", driveId);
            return null;
        }
    }

    @Override
    public boolean createSong(SongRequestDTO songRequestDTO) {
        try {

            songRequestDTO.setGoogleDriveFileId(
                    googleDriveService.uploadSongFile(songRequestDTO.getMp3Files().getFirst()));

            SongEntity song = songConverter.toEntity(songRequestDTO);
            song.setTags(tagService.generateTagsForSong(song));
            songRepository.save(song);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int importSongsFromGoogleDrive() {
        List<GoogleDriveService.DriveFileBundle> songBundles = googleDriveService.loadAllSongsWithMetadata();

        int importedCount = 0;

        for (GoogleDriveService.DriveFileBundle bundle : songBundles) {
            try {
                // Create song request DTO
                SongRequestDTO songRequestDTO = new SongRequestDTO();

                // Set Google Drive file ID
                songRequestDTO.setGoogleDriveFileId(bundle.getSongFile().getId());

                // Create song entity
                SongEntity song = songConverter.toEntity(songRequestDTO);

                // Save song
                SongEntity savedSong = songRepository.save(song);

                try {
                    tagService.generateTagsForSong(savedSong);
                } catch (Exception e) {
                    log.warn("Failed to generate tags for song {}: {}", savedSong.getTitle(), e.getMessage());
                }

                importedCount++;

            } catch (Exception e) {
                log.error("Failed to import song {}: {}", bundle.getSongFile().getName(), e.getMessage(), e);
            }
        }

        log.info("Imported {} songs from Google Drive out of {} found", importedCount, songBundles.size());
        return importedCount;
    }


    @Override
    public List<SongDTO> searchSongs(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Normalize the original query
        String normalizedQuery = normalizeText(query);

        // Generate alternative forms of the query
        Set<String> queryVariants = generateQueryVariants(normalizedQuery);

        // Combine results from all query variants with deduplication
        Set<SongEntity> results = new LinkedHashSet<>();

        // Search by exact title match first (higher priority)
        for (String variant : queryVariants) {
            List<SongEntity> exactMatches = songRepository.findByTitleContainingIgnoreCase(variant);
            results.addAll(exactMatches);

            if (results.size() >= limit) {
                break;
            }
        }

        if (results.size() < limit) {
            List<SongEntity> fullTextResults = performFullTextSearch(queryVariants, limit - results.size());
            results.addAll(fullTextResults);
        }

        // Convert to DTOs
        return results.stream()
                .limit(limit)
                .map(songConverter::toDTO)
                .collect(Collectors.toList());
    }

    private String normalizeText(String text) {
        // Remove accents and convert to lowercase
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();

        // Remove special characters except spaces
        return normalized.replaceAll("[^\\p{Alnum}\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Set<String> generateQueryVariants(String query) {
        Set<String> variants = new HashSet<>();
        variants.add(query);

        // Add romaji/kana variants if Japanese characters are detected
        if (containsJapanese(query)) {
            variants.add(kanaToLatin.transliterate(query));
        } else {
            variants.add(latinToKana.transliterate(query));
        }

        // Add variants with different word separators
        variants.add(query.replace(" ", ""));  // No spaces

        // Add word-by-word variants for partial matching
        String[] words = query.split("\\s+");
        if (words.length > 1) {
            for (String word : words) {
                if (word.length() > 2) {  // Only add meaningful words
                    variants.add(word);
                }
            }
        }

        return variants;
    }

    private boolean containsJapanese(String text) {
        return text.matches(".*[\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}].*");
    }

    private List<SongEntity> performFullTextSearch(Set<String> queryVariants, int limit) {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT DISTINCT s FROM SongEntity s ");

        queryBuilder.append("LEFT JOIN s.artists a ");
        queryBuilder.append("LEFT JOIN s.album alb ");
        queryBuilder.append("WHERE ");

        List<String> conditions = new ArrayList<>();
        for (int i = 0; i < queryVariants.size(); i++) {
            conditions.add("(LOWER(s.title) LIKE :query" + i + " OR " +
                    "LOWER(a.stageName) LIKE :query" + i + " OR " +
                    "LOWER(alb.title) LIKE :query" + i + ")");
        }

        queryBuilder.append(String.join(" OR ", conditions));

        queryBuilder.append(" ORDER BY CASE WHEN LOWER(s.title) = :exactTitle THEN 0 " +
                "WHEN LOWER(s.title) LIKE :startsWithTitle THEN 1 " +
                "WHEN EXISTS (SELECT 1 FROM s.artists a1 WHERE LOWER(a1.stageName) = :exactArtist) THEN 2 " +
                "ELSE 3 END, s.playCount DESC");

        try {
            Query query = entityManager.createQuery(queryBuilder.toString());

            int i = 0;
            String exactTitle = null;
            String exactArtist = null;

            for (String variant : queryVariants) {
                query.setParameter("query" + i, "%" + variant + "%");
                if (i == 0) {
                    exactTitle = variant.toLowerCase();
                    exactArtist = variant.toLowerCase();
                }
                i++;
            }

            query.setParameter("exactTitle", exactTitle);
            query.setParameter("startsWithTitle", exactTitle + "%");
            query.setParameter("exactArtist", exactArtist);

            query.setMaxResults(limit);

            @SuppressWarnings("unchecked")
            List<SongEntity> results = query.getResultList();
            return results;
        } catch (Exception e) {
            log.error("Error performing full text search: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}

