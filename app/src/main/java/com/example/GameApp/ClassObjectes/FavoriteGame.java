package com.example.GameApp.ClassObjectes;

public class FavoriteGame {
    private String title;
    private String cover_url;
    private int rating;
    private int coverId;

    public FavoriteGame() {
        // Constructor vacío requerido por Firestore
    }

    public FavoriteGame(String title, String cover_url, int rating, int coverId) {
        this.title = title;
        this.cover_url = cover_url;
        this.rating = rating;
        this.coverId = coverId;
    }

    // Getters y Setters

    public String getTitle() {
        return title;
    }

    public String getCover_url() {
        return cover_url;
    }

    public int getRating() {
        return rating;
    }

    public int getCoverId() {
        return coverId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCover_url(String cover_url) {
        this.cover_url = cover_url;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setCoverId(int coverId) {
        this.coverId = coverId;
    }
}
