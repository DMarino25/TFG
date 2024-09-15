package com.example.GameApp.ClassObjectes;

import java.util.ArrayList;
import java.util.List;

public class Platforms {

    private int id;
    private String abbreviation;

    private String alternative_name;
    private String name;
    private String slug;
    private String checksum;
    private String url;
    private int category;
    private int created_at;
    private List<Integer> versions = new ArrayList<>();
    private List<Integer> websites = new ArrayList<>();

    public int getId() {
        return id;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getAlternative_name() {
        return alternative_name;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getUrl() {
        return url;
    }

    public int getCategory() {
        return category;
    }

    public int getCreated_at() {
        return created_at;
    }

    public List<Integer> getVersions() {
        return versions;
    }

    public List<Integer> getWebsites() {
        return websites;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void setAlternative_name(String alternative_name) {
        this.alternative_name = alternative_name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public void setVersions(List<Integer> versions) {
        this.versions = versions;
    }

    public void setWebsites(List<Integer> websites) {
        this.websites = websites;
    }
}
