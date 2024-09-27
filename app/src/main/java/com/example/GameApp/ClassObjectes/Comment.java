package com.example.GameApp.ClassObjectes;

public class Comment {

    private String authorName;
    private String authorImage;
    private String commentText;

    public Comment(String authorName, String authorImage, String commentText) {
        this.authorName = authorName;
        this.authorImage = authorImage;
        this.commentText = commentText;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorImage() {
        return authorImage;
    }

    public String getCommentText() {
        return commentText;
    }
}
