package com.example.GameApp.ClassObjectes;

import java.util.List;

public class Game {

    private int id;
    private List<Integer> artworks;
    private String name;

    // Getters y Setters
    public List<Integer> getArtworks() { return artworks; }
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }


    public void setId(int id) { this.id = id; }


    public void setArtworks(List<Integer> artworks) { this.artworks = artworks; }
}

