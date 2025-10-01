package dev.kvnmtz.headdatabase.data;

import com.google.gson.JsonParser;
import dev.kvnmtz.headdatabase.HeadDatabase;
import dev.kvnmtz.headdatabase.model.Head;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HeadDataManager {

    private static volatile HeadDataManager instance;
    private static final String HEAD_DATABASE_URL
            = "https://raw.githubusercontent.com/TheSilentPro/heads/refs/heads/main/heads.json";

    private final Map<String, List<Head>> headsByCategory = new ConcurrentHashMap<>();

    private final HttpClient httpClient;
    private volatile boolean isLoaded = false;
    private volatile boolean isLoading = false;

    private HeadDataManager() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public static HeadDataManager getInstance() {
        if (instance == null) {
            synchronized (HeadDataManager.class) {
                if (instance == null) {
                    instance = new HeadDataManager();
                }
            }
        }
        return instance;
    }

    public CompletableFuture<Void> initialize() {
        if (isLoaded || isLoading) {
            return CompletableFuture.completedFuture(null);
        }

        synchronized (this) {
            if (isLoaded || isLoading) {
                return CompletableFuture.completedFuture(null);
            }
            isLoading = true;
        }

        return CompletableFuture.runAsync(() -> {
            try {
                HeadDatabase.LOGGER.info("Fetching head data from {}", HEAD_DATABASE_URL);
                loadHeadData();
                isLoaded = true;
                HeadDatabase.LOGGER.info("Successfully loaded head database with {} categories and {} total heads",
                        headsByCategory.size(),
                        headsByCategory.values().stream().mapToInt(List::size).sum());
            } catch (Exception e) {
                HeadDatabase.LOGGER.error("Failed to load head database", e);
            } finally {
                isLoading = false;
            }
        });
    }

    private void loadHeadData() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(HEAD_DATABASE_URL))
                .GET()
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch head data: HTTP " + response.statusCode());
        }

        var jsonContent = response.body();
        parseAndStoreHeads(jsonContent);
    }

    private void parseAndStoreHeads(String jsonContent) {
        var jsonArray = JsonParser.parseString(jsonContent).getAsJsonArray();
        Map<String, List<Head>> tempMap = new HashMap<>();

        for (var i = 0; i < jsonArray.size(); i++) {
            var headObj = jsonArray.get(i).getAsJsonObject();

            var name = headObj.get("name").getAsString();
            var texture = headObj.get("texture").getAsString();
            var category = headObj.get("category").getAsString();

            List<String> tags = new ArrayList<>();
            if (headObj.has("tags") && headObj.get("tags").isJsonArray()) {
                var tagsArray = headObj.getAsJsonArray("tags");
                for (var j = 0; j < tagsArray.size(); j++) {
                    tags.add(tagsArray.get(j).getAsString());
                }
            }

            var head = new Head(name, texture, category, tags);

            tempMap.computeIfAbsent(category, k -> new ArrayList<>()).add(head);
        }

        headsByCategory.clear();
        headsByCategory.putAll(tempMap);
    }

    public List<Head> getHeadsByCategory(String category) {
        return headsByCategory.getOrDefault(category, Collections.emptyList());
    }

    public Set<String> getCategories() {
        return new HashSet<>(headsByCategory.keySet());
    }

    public List<Head> getAllHeads() {
        return headsByCategory.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Head> searchHeads(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        var searchTerms = query.toLowerCase().trim().split("\\s+");

        return getAllHeads().stream().filter(head -> matchesAllSearchTerms(head, searchTerms)).toList();
    }

    private boolean matchesAllSearchTerms(Head head, String[] searchTerms) {
        var lowerName = head.name().toLowerCase();
        var lowerTags = head.tags().stream().map(String::toLowerCase).toList();

        for (var term : searchTerms) {
            var termMatches = lowerName.contains(term) || lowerTags.stream().anyMatch(tag -> tag.contains(term));

            if (!termMatches) {
                return false;
            }
        }

        return true;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean isLoading() {
        return isLoading;
    }
}