import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

public class UploadImagesToS3 {

    static String bucketName = "a2-music-images-s4093788";

    public static void main(String[] args) throws Exception {

        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        // ── Load JSON file ─────────────────────────────────────────────────
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("2026a2_songs.json"));
        JsonNode songs = root.get("songs");

        // ── Track already uploaded images to avoid duplicates ──────────────
        // Multiple songs share the same artist image, so we only upload each once
        Set<String> uploaded = new HashSet<>();

        int count = 0;
        System.out.println("Downloading and uploading artist images to S3...");

        for (JsonNode song : songs) {
            String imageUrl = song.get("img_url").asText();

            // Extract filename from URL e.g. "TaylorSwift.jpg"
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // Skip if already uploaded
            if (uploaded.contains(fileName)) {
                System.out.println("[SKIP] Already uploaded: " + fileName);
                continue;
            }

            // Download image to a temp file
            File tempFile = File.createTempFile("img_", "_" + fileName);
            try (InputStream in = new URL(imageUrl).openStream()) {
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Upload to S3
            s3.putObject(new PutObjectRequest(bucketName, fileName, tempFile));

            uploaded.add(fileName);
            count++;
            System.out.println("[" + count + "] Uploaded: " + fileName);

            // Clean up temp file
            tempFile.delete();
        }

        System.out.println("\nDone! " + count + " images uploaded to S3 bucket: " + bucketName);
    }
}