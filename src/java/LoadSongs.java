import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public class LoadSongs {

    static String tableName = "music";

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(tableName);

        // ── Load JSON file ─────────────────────────────────────────────────
        // Place 2026a2_songs.json in your project root (next to pom.xml)
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("2026a2_songs.json"));
        JsonNode songs = root.get("songs");

        System.out.println("Loading " + songs.size() + " songs into music table...");

        int count = 0;
        for (JsonNode song : songs) {
            String title    = song.get("title").asText();
            String artist   = song.get("artist").asText();
            String year     = song.get("year").asText();
            String album    = song.get("album").asText();
            String imageUrl = song.get("img_url").asText();

            // Key schema analysis:
            // - title alone is NOT unique (e.g. "Bad Blood" by 2 artists)
            // - title + artist is NOT unique (e.g. "Delicate" by Taylor Swift on 2 albums)
            // - title + artist + album IS unique (137/137 confirmed)
            // Solution: partition key = artist, sort key = title#album (composite)
            // This guarantees no songs are overwritten during import
            String sortKey = title + "#" + album;

            table.putItem(new Item()
                    .withPrimaryKey("artist", artist, "title", sortKey)
                    .withString("song_title", title)   // original title for display
                    .withString("year",       year)
                    .withString("album",      album)
                    .withString("image_url",  imageUrl)
            );

            count++;
            System.out.println("[" + count + "] Loaded: " + artist + " - " + title + " (" + album + ")");
        }

        System.out.println("\nDone! " + count + " songs loaded into the music table.");
    }
}