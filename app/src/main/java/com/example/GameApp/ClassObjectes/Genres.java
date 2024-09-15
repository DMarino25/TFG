package com.example.GameApp.ClassObjectes;

public class Genres {


    private int id;
    private int created_at;

    private String name;
    private String slug;
    private int updated_at;
    private String url;
    private String checksum;

    public int getId() {
        return id;
    }

    public int getCreated_at() {
        return created_at;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    public String getUrl() {
        return url;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }



}
