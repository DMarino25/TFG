package com.example.GameApp.ClassObjectes;

public class User {
    private String uid;
    private String name;
    private String photoUrl;


    public User() {
    }

    public User(String uid, String name, String photoUrl) {
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    // Getters y setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User usuari = (User) obj;
        return uid.equals(usuari.uid);
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }
}
