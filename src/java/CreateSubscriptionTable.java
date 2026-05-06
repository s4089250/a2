import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.Arrays;

public class CreateSubscriptionTable {

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);
        String tableName = "subscription";

        try {
            client.describeTable(tableName);
            System.out.println("Table already exists: " + tableName);
            return;
        } catch (ResourceNotFoundException e) {
            // continue
        }

        System.out.println("Creating subscription table...");

        Table table = dynamoDB.createTable(
                tableName,
                Arrays.asList(
                        new KeySchemaElement("email", KeyType.HASH),  // who subscribed
                        new KeySchemaElement("title", KeyType.RANGE)  // title#album (unique song key)
                ),
                Arrays.asList(
                        new AttributeDefinition("email", ScalarAttributeType.S),
                        new AttributeDefinition("title", ScalarAttributeType.S)
                ),
                new ProvisionedThroughput(10L, 10L)
        );

        table.waitForActive();
        System.out.println("Done! Subscription table created.");
    }
}