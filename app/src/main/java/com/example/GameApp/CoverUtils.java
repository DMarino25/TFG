package com.example.GameApp;

public class CoverUtils {

    // Method to construct the image URL from imageId and size
    public static String constructImageUrl(String imageId, String size) {
        return "https://images.igdb.com/igdb/image/upload/" + size + "/" + imageId + ".jpg";
    }

    // Method to extract imageId from a given URL
    public static String extractImageId(String url) {
        // Find the last '/' and '.' in the string
        int lastSlashIndex = url.lastIndexOf('/');
        int lastDotIndex = url.lastIndexOf('.');

        // Extract the substring between '/' and '.'
        if (lastSlashIndex != -1 && lastDotIndex != -1 && lastDotIndex > lastSlashIndex) {
            return url.substring(lastSlashIndex + 1, lastDotIndex);
        }

        return ""; // Return an empty string if the format is unexpected
    }
}
