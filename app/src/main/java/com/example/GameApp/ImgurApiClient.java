package com.example.GameApp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ImgurApiClient {

    private static final String CLIENT_ID = BuildConfig.CLIENT_ID_IMGUR;
    private static final String IMGUR_UPLOAD_URL = "https://api.imgur.com/3/image";

    // Executor service for running background tasks
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Method to upload image to Imgur
    public static void uploadImageToImgur(InputStream imageStream, UploadCallback callback) {
        executor.execute(() -> {
            try {
                // Prepare the image file for upload
                byte[] imageBytes = new byte[imageStream.available()];
                imageStream.read(imageBytes);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/*"));
                Request request = new Request.Builder()
                        .url(IMGUR_UPLOAD_URL)
                        .addHeader("Authorization", "Client-ID " + CLIENT_ID)
                        .post(body)
                        .build();

                // Perform the upload request
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("Imgur", "responseData:" + responseData);

                    // Parse the Imgur URL from the response (in JSON format)
                    String imgurUrl = parseImgurUrl(responseData);

                    // Pass the URL to the callback
                    callback.onUploadSuccess(imgurUrl);
                } else {
                    callback.onUploadFailure("Failed to upload image: " + response.message());
                }

            } catch (IOException e) {
                callback.onUploadFailure("Failed to upload image: " + e.getMessage());
            }
        });
    }

    // A helper method to parse the Imgur URL from the response
    private static String parseImgurUrl(String responseData) {
        int linkIndex = responseData.indexOf("\"link\":\"");
        if (linkIndex != -1) {
            int startIndex = linkIndex + 8;
            int endIndex = responseData.indexOf("\"", startIndex);
            return responseData.substring(startIndex, endIndex);
        }
        return null;
    }

    // Callback interface to pass results back to the caller
    public interface UploadCallback {
        void onUploadSuccess(String imgurUrl);
        void onUploadFailure(String errorMessage);
    }
}
