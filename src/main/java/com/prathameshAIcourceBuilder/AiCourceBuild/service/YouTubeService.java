package com.prathameshAIcourceBuilder.AiCourceBuild.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class YouTubeService {

    private final WebClient webClient;
    private final String apiKey;
    private final String searchUrl;

    public YouTubeService(@Value("${youtube.api.key:}") String apiKey,
                         @Value("${youtube.search.url:https://www.googleapis.com/youtube/v3/search}") String searchUrl) {
        this.apiKey = apiKey;
        this.searchUrl = searchUrl;
        this.webClient = WebClient.builder()
            .baseUrl(searchUrl)
            .build();
    }

    public String searchVideoUrl(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            return "https://www.youtube.com/results?search_query=" + query.replace(" ", "+");
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(searchUrl)
                .queryParam("part", "snippet")
                .queryParam("q", query)
                .queryParam("maxResults", 1)
                .queryParam("type", "video")
                .queryParam("key", apiKey)
                .build()
                .toUri();

            Mono<Map> responseMono = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10));

            Map result = responseMono.block();

            if (result == null) {
                return createSearchUrl(query);
            }

            if (result.containsKey("error")) {
                return createSearchUrl(query);
            }

            Object itemsObj = result.get("items");
            if (itemsObj instanceof List && !((List<?>) itemsObj).isEmpty()) {
                Object first = ((List<?>) itemsObj).get(0);
                if (first instanceof Map) {
                    Object idObj = ((Map<?, ?>) first).get("id");
                    if (idObj instanceof Map) {
                        Object videoId = ((Map<?, ?>) idObj).get("videoId");
                        if (videoId != null) {
                            return "https://www.youtube.com/watch?v=" + videoId.toString();
                        }
                    }
                }
            }

            return createSearchUrl(query);

        } catch (Exception ex) {
            return createSearchUrl(query);
        }
    }

    private String createSearchUrl(String query) {
        String searchQuery = query.replace(" ", "+");
        return "https://www.youtube.com/results?search_query=" + searchQuery;
    }
}