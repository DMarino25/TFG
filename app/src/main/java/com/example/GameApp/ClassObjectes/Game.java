package com.example.GameApp.ClassObjectes;

import java.util.List;

public class Game {

    private int id;
    private List<Integer> artworks;
    private String name;
    private List<Integer> ageRatings;
    private double aggregatedRating;
    private int aggregatedRatingCount;
    private List<Integer> alternativeNames;
    private int category;
    private Cover cover;
    private long createdAt;
    private List<Integer> externalGames;
    private long firstReleaseDate;
    private List<Integer> franchises;
    private List<Integer> gameEngines;
    private List<Integer> gameModes;
    private List<Integer> genres;
    private int hypes;
    private List<Integer> involvedCompanies;
    private List<Integer> keywords;
    private List<Integer> multiplayerModes;
    private double rating;
    private int ratingCount;
    private List<Integer> releaseDates;
    private List<Integer> screenshots;
    private List<Integer> similarGames;
    private String slug;
    private String summary;
    private List<Integer> tags;
    private List<Integer> platforms;
    private List<Integer> themes;
    private double totalRating;
    private int totalRatingCount;
    private long updatedAt;
    private String url;
    private List<Integer> videos;
    private List<Integer> websites;

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Cover getCover() { return cover; }
    public void setCover(Cover cover) { this.cover = cover; }
    public List<Integer> getArtworks() { return artworks; }
    public void setArtworks(List<Integer> artworks) { this.artworks = artworks; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Integer> getAgeRatings() { return ageRatings; }
    public void setAgeRatings(List<Integer> ageRatings) { this.ageRatings = ageRatings; }

    public double getAggregatedRating() { return aggregatedRating; }
    public void setAggregatedRating(double aggregatedRating) { this.aggregatedRating = aggregatedRating; }

    public int getAggregatedRatingCount() { return aggregatedRatingCount; }
    public void setAggregatedRatingCount(int aggregatedRatingCount) { this.aggregatedRatingCount = aggregatedRatingCount; }

    public List<Integer> getAlternativeNames() { return alternativeNames; }
    public void setAlternativeNames(List<Integer> alternativeNames) { this.alternativeNames = alternativeNames; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public List<Integer> getExternalGames() { return externalGames; }
    public void setExternalGames(List<Integer> externalGames) { this.externalGames = externalGames; }

    public long getFirstReleaseDate() { return firstReleaseDate; }
    public void setFirstReleaseDate(long firstReleaseDate) { this.firstReleaseDate = firstReleaseDate; }

    public List<Integer> getFranchises() { return franchises; }
    public void setFranchises(List<Integer> franchises) { this.franchises = franchises; }

    public List<Integer> getGameEngines() { return gameEngines; }
    public void setGameEngines(List<Integer> gameEngines) { this.gameEngines = gameEngines; }

    public List<Integer> getGameModes() { return gameModes; }
    public void setGameModes(List<Integer> gameModes) { this.gameModes = gameModes; }

    public List<Integer> getGenres() { return genres; }
    public void setGenres(List<Integer> genres) { this.genres = genres; }

    public List<Integer> getPlatforms() { return genres; }

    public void setPlatforms(List<Integer> platforms) { this.platforms = platforms; }
    public int getHypes() { return hypes; }
    public void setHypes(int hypes) { this.hypes = hypes; }

    public List<Integer> getInvolvedCompanies() { return involvedCompanies; }
    public void setInvolvedCompanies(List<Integer> involvedCompanies) { this.involvedCompanies = involvedCompanies; }

    public List<Integer> getKeywords() { return keywords; }
    public void setKeywords(List<Integer> keywords) { this.keywords = keywords; }

    public List<Integer> getMultiplayerModes() { return multiplayerModes; }
    public void setMultiplayerModes(List<Integer> multiplayerModes) { this.multiplayerModes = multiplayerModes; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public List<Integer> getReleaseDates() { return releaseDates; }
    public void setReleaseDates(List<Integer> releaseDates) { this.releaseDates = releaseDates; }

    public List<Integer> getScreenshots() { return screenshots; }
    public void setScreenshots(List<Integer> screenshots) { this.screenshots = screenshots; }

    public List<Integer> getSimilarGames() { return similarGames; }
    public void setSimilarGames(List<Integer> similarGames) { this.similarGames = similarGames; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<Integer> getTags() { return tags; }
    public void setTags(List<Integer> tags) { this.tags = tags; }

    public List<Integer> getThemes() { return themes; }
    public void setThemes(List<Integer> themes) { this.themes = themes; }

    public double getTotalRating() { return totalRating; }
    public void setTotalRating(double totalRating) { this.totalRating = totalRating; }

    public int getTotalRatingCount() { return totalRatingCount; }
    public void setTotalRatingCount(int totalRatingCount) { this.totalRatingCount = totalRatingCount; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public List<Integer> getVideos() { return videos; }
    public void setVideos(List<Integer> videos) { this.videos = videos; }

    public List<Integer> getWebsites() { return websites; }
    public void setWebsites(List<Integer> websites) { this.websites = websites; }
}
