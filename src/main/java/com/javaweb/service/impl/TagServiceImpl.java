package com.javaweb.service.impl;

import com.javaweb.entity.ArtistEntity;
import com.javaweb.entity.SongEntity;
import com.javaweb.entity.TagEntity;
import com.javaweb.entity.TagEntity.TagType;
import com.javaweb.repository.SongRepository;
import com.javaweb.repository.TagRepository;
import com.javaweb.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final OllamaChatModel chatModel;
    private final SongRepository songRepository;
    private final TagRepository tagRepository;

    // Common keywords for tag type classification
    private static final Map<TagType, List<String>> TAG_TYPE_KEYWORDS = Map.of(
            TagType.GENRE, List.of("rock", "pop", "jazz", "hiphop", "rap", "country", "electronic", "metal", "folk", "classical", "blues", "reggae", "r&b"),
            TagType.MOOD, List.of("happy", "sad", "melancholic", "angry", "nostalgic", "uplifting", "romantic", "calm", "upbeat", "passionate", "dark", "dreamy"),
            TagType.INSTRUMENT, List.of("guitar", "piano", "drums", "bass", "violin", "saxophone", "trumpet", "synth", "vocal", "orchestra", "acoustic"),
            TagType.TEMPO, List.of("fast", "slow", "moderate", "upbeat", "downtempo", "mid-tempo", "ambient", "driving"),
            TagType.ATMOSPHERE, List.of("chill", "intense", "atmospheric", "raw", "mellow", "ethereal", "gloomy", "bright", "warm", "cold", "spacey"),
            TagType.THEME, List.of("love", "breakup", "politics", "social", "party", "summer", "winter", "holiday", "spiritual", "religious"),
            TagType.CONTEXT, List.of("dance", "workout", "study", "sleep", "background", "driving", "dinner", "meditation"),
            TagType.ERA, List.of("80s", "90s", "2000s", "vintage", "modern", "classic", "retro", "contemporary"),
            TagType.LANGUAGE, List.of("english", "spanish", "french", "korean", "japanese", "chinese", "instrumental")
    );

    @Override
    public Set<TagEntity> generateTagsForSong(SongEntity song) {
        try {
            // Prepare song information for analysis
            String songInfo = String.format(
                    "Title: %s, Artist(s): %s, Album: %s, Lyrics: %s",
                    song.getTitle(),
                    song.getArtists().stream().map(ArtistEntity::getStageName).collect(Collectors.joining(", ")),
                    song.getAlbum() != null ? song.getAlbum().getTitle() : "Unknown",
                    song.getLyrics() != null ? song.getLyrics().getContent() : "No lyrics available"
            );

            // Enhanced prompt to categorize tags
            String systemPrompt = """
                    You are a music classification expert who specializes in Spotify-style genre tagging.
                    Spotify uses specific genre taxonomies with multi-level categories (e.g., "indie folk" rather than separate "indie" and "folk" tags).
                    
                    Analyze the song information and return appropriate music tags that match Spotify's genre system.
                    Return tags in this format: CATEGORY:tag, CATEGORY:tag
                    
                    Valid categories are: GENRE, MOOD, INSTRUMENT, THEME, TEMPO, ATMOSPHERE, CONTEXT, ERA, LANGUAGE
                    
                    For GENRE tags, prefer compound descriptors like "indie-pop", "alternative rock", "deep house", "EDM trap"
                    rather than broad categories like "pop" or "rock".
                    
                    Examples of Spotify-style genre tags:
                    - "indie folk" not just "folk"
                    - "modern classical" not just "classical"  
                    - "deep house" not just "house"
                    - "melodic dubstep" not just "dubstep"
                    
                    Return a maximum of 5 tags total.
                    Do not return explanations, just the categorized tags as described.
                    """;

            var systemMessage = new SystemPromptTemplate(systemPrompt).createMessage();
            var userMessage = new UserMessage(songInfo);

            var prompt = new Prompt(List.of(systemMessage, userMessage));
            var response = chatModel.call(prompt);

            String tagString = response.getResult().getOutput().getText().trim();
            log.info("Generated tags for song '{}': {}", song.getTitle(), tagString);
            log.info("Song info: '{}'", songInfo);

            // Process and persist the tags
            return processAndSaveTags(tagString);

        } catch (Exception e) {
            log.error("Error generating tags with Ollama model: {}", e.getMessage(), e);
            return new HashSet<>();
        }
    }

    private Set<TagEntity> processAndSaveTags(String tagString) {
        Set<TagEntity> resultTags = new HashSet<>();

        // Pattern to match category:tag format (e.g., "GENRE:rock")
        Pattern pattern = Pattern.compile("(\\w+)\\s*:\\s*([\\w\\s-]+)");
        Matcher matcher = pattern.matcher(tagString);

        while (matcher.find()) {
            try {
                String categoryStr = matcher.group(1).trim().toUpperCase();
                String tagName = matcher.group(2).trim().toLowerCase();

                if (!tagName.isEmpty()) {
                    TagType tagType;

                    try {
                        tagType = TagType.valueOf(categoryStr);
                    } catch (IllegalArgumentException e) {
                        tagType = inferTagTypeFromName(tagName);
                    }

                    // Find existing tag or create a new one
                    TagType finalTagType = tagType;
                    TagEntity tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                TagEntity newTag = new TagEntity();
                                newTag.setName(tagName);
                                newTag.setTagtype(finalTagType);
                                newTag.setDescription("Auto-generated tag");
                                return tagRepository.save(newTag);
                            });

                    if (tag.getTagtype() == null) {
                        tag.setTagtype(tagType);
                        tagRepository.save(tag);
                    }

                    resultTags.add(tag);
                }
            } catch (Exception e) {
                log.warn("Error processing tag: {}", e.getMessage());
            }
        }

        if (resultTags.isEmpty()) {
            String[] tags = tagString.split(",");
            for (String tagName : tags) {
                tagName = tagName.trim().toLowerCase();
                if (!tagName.isEmpty()) {
                    // Infer the tag type from the name
                    TagType tagType = inferTagTypeFromName(tagName);

                    // Find or create tag
                    String finalTagName = tagName;
                    TagEntity tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> {
                                TagEntity newTag = new TagEntity();
                                newTag.setName(finalTagName);
                                newTag.setTagtype(tagType);
                                newTag.setDescription("Auto-generated tag");
                                return tagRepository.save(newTag);
                            });

                    resultTags.add(tag);
                }
            }
        }

        return resultTags;
    }

    /**
     * Infer tag type from tag name based on common keywords
     */
    private TagType inferTagTypeFromName(String tagName) {
        // Default to THEME if we can't determine
        TagType inferredType = TagType.THEME;

        // Check against our keyword lists
        for (Map.Entry<TagType, List<String>> entry : TAG_TYPE_KEYWORDS.entrySet()) {
            if (entry.getValue().stream().anyMatch(keyword ->
                    tagName.equals(keyword) || tagName.startsWith(keyword + " ") ||
                            tagName.endsWith(" " + keyword) || tagName.contains(" " + keyword + " "))) {
                return entry.getKey();
            }
        }

        return inferredType;
    }

    @Override
    public void autoTagSongs(List<Long> songIds) {
        songIds.forEach(songId -> {
            songRepository.findById(songId).ifPresent(song -> {
                Set<TagEntity> tags = generateTagsForSong(song);
                if (!tags.isEmpty()) {
                    song.getTags().addAll(tags);
                    songRepository.save(song);

                    // Log the types of tags added
                    Map<TagType, Long> tagTypeCount = tags.stream()
                            .collect(Collectors.groupingBy(
                                    TagEntity::getTagtype,
                                    Collectors.counting()
                            ));

                    log.info("Added {} tags to song '{}' - Types: {}",
                            tags.size(), song.getTitle(), tagTypeCount);
                }
            });
        });
    }
}