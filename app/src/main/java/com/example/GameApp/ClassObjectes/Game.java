package com.example.GameApp.ClassObjectes;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Game {

    private int id;
    private String name;
    private String summary;
    private Cover cover;

    @SerializedName("genres")
    private List<Genres> genres;

    @SerializedName("platforms")
    private List<Platforms> platforms;

    public List<Keywords> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keywords> keywords) {
        this.keywords = keywords;
    }

    @SerializedName("involved_companies")
    private List<InvolvedCompanies> involvedCompanies;
    @SerializedName("first_release_date")
    private long first_release_date;
    @SerializedName("keywords")
    private List<Keywords> keywords;

    public long getFirstReleaseDate() {
        return first_release_date;
    }

    public void setFirstReleaseDate(long first_release_date) {
        this.first_release_date = first_release_date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Cover getCover() {
        return cover;
    }

    public void setCover(Cover cover) {
        this.cover = cover;
    }

    public List<Genres> getGenres() {
        return genres;
    }

    public void setGenres(List<Genres> genres) {
        this.genres = genres;
    }

    public List<Platforms> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Platforms> platforms) {
        this.platforms = platforms;
    }

    public List<InvolvedCompanies> getInvolvedCompanies() {
        return involvedCompanies;
    }

    public void setInvolvedCompanies(List<InvolvedCompanies> involvedCompanies) {
        this.involvedCompanies = involvedCompanies;
    }
}
