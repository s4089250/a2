import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.Arrays;

public class CreateMusicTable {

    static String tableName = "music";

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        // ── Check if table already exists ─────────────────────────────────
        try {
            client.describeTable(tableName);
            System.out.println("Table already exists: " + tableName);
            return;
        } catch (ResourceNotFoundException e) {
            // Table does not exist yet; continue with creation.
        }

        // ── Create the music table ─────────────────────────────────────────
        // Key schema analysis:
        //   - title alone is NOT unique (e.g. "Bad Blood" by 2 different artists)
        //   - title + artist is NOT unique (e.g. "Delicate" by Taylor Swift on 2 albums)
        //   - Partition key = artist, Sort key = title
        //   - For songs with same title+artist (different albums), album is stored as attribute
        //   - GSI: year-artist-index  (query by year, filter by artist)
        //   - LSI: artist-album-index (query by artist, filter by album) -- same partition key

        try {
            System.out.println("Attempting to create music table; please wait...");

            CreateTableRequest request = new CreateTableRequest()
                    .withTableName(tableName)

                    // Primary key: artist (PK) + title (SK)
                    .withKeySchema(
                            new KeySchemaElement("artist", KeyType.HASH),
                            new KeySchemaElement("title", KeyType.RANGE)
                    )

                    // Attribute definitions (only indexed attributes go here)
                    .withAttributeDefinitions(
                            new AttributeDefinition("artist", ScalarAttributeType.S),
                            new AttributeDefinition("title",  ScalarAttributeType.S),
                            new AttributeDefinition("year",   ScalarAttributeType.S),
                            new AttributeDefinition("album",  ScalarAttributeType.S)
                    )

                    // LSI: query by artist (same PK) + album as sort key
                    // Useful for: "find all songs by Taylor Swift in album Fearless"
                    .withLocalSecondaryIndexes(
                            new LocalSecondaryIndex()
                                    .withIndexName("artist-album-index")
                                    .withKeySchema(
                                            new KeySchemaElement("artist", KeyType.HASH),
                                            new KeySchemaElement("album",  KeyType.RANGE)
                                    )
                                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                    )

                    // GSI: query by year (PK) + artist (SK)
                    // Useful for: "find all songs by Jimmy Buffett in 1974"
                    .withGlobalSecondaryIndexes(
                            new GlobalSecondaryIndex()
                                    .withIndexName("year-artist-index")
                                    .withKeySchema(
                                            new KeySchemaElement("year",   KeyType.HASH),
                                            new KeySchemaElement("artist", KeyType.RANGE)
                                    )
                                    .withProjection(new Projection().withProjectionType(ProjectionType.ALL))
                                    .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L))
                    )

                    .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L));

            Table table = dynamoDB.createTable(request);
            table.waitForActive();
            System.out.println("Success. Table status: " + table.getDescription().getTableStatus());
            System.out.println("Music table created with:");
            System.out.println("  - Partition key: artist");
            System.out.println("  - Sort key:      title");
            System.out.println("  - LSI: artist-album-index");
            System.out.println("  - GSI: year-artist-index");

        } catch (InterruptedException e) {
            System.err.println("Unable to create table: ");
            System.err.println(e.getMessage());
        }
    }
}