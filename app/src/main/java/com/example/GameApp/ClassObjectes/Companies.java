package com.example.GameApp.ClassObjectes;

public class Companies {
    private int id;
    private int changeDateCategory;
    private long createdAt;
    private String name;
    private String slug;
    private int startDateCategory;
    private long updatedAt;
    private String url;
    private String checksum;

    public Companies(int id, int changeDateCategory, long createdAt, String name, String slug, int startDateCategory, long updatedAt, String url, String checksum) {
        this.id = id;
        this.changeDateCategory = changeDateCategory;
        this.createdAt = createdAt;
        this.name = name;
        this.slug = slug;
        this.startDateCategory = startDateCategory;
        this.updatedAt = updatedAt;
        this.url = url;
        this.checksum = checksum;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getChangeDateCategory() {
        return changeDateCategory;
    }

    public void setChangeDateCategory(int changeDateCategory) {
        this.changeDateCategory = changeDateCategory;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public int getStartDateCategory() {
        return startDateCategory;
    }

    public void setStartDateCategory(int startDateCategory) {
        this.startDateCategory = startDateCategory;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
