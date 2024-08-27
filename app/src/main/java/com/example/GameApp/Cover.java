package com.example.GameApp;

public class Cover {
    private int id;
    private int game;
    private int height;
    private String imageId;
    private String url;
    private int width;
    private String checksum;

    public Cover(int id, int game, int height, String imageId, String url, int width, String checksum) {
        this.id = id;
        this.game = game;
        this.height = height;
        this.imageId = imageId;
        this.url = url;
        this.width = width;
        this.checksum = checksum;
    }

    // Getters y setters

    public int getId() {
        return id;
    }

    public int getGame() {
        return game;
    }

    public int getHeight() {
        return height;
    }

    public String getImageId() {
        return imageId;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public String getChecksum() {
        return checksum;
    }
}