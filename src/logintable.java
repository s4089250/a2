import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

import java.util.Arrays;

public class CreateLoginTable {

    static String tableName = "login";

    public static void main(String[] args) throws Exception {

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .withCredentials(new ProfileCredentialsProvider("default"))
                .build();

        DynamoDB dynamoDB = new DynamoDB(client);

        // ── Step 1: Create the table if it doesn't exist ──────────────────
        try {
            client.describeTable(tableName);
            System.out.println("Table already exists: " + tableName);
        } catch (ResourceNotFoundException e) {
            // Table does not exist yet; continue with creation.
            try {
                System.out.println("Attempting to create table; please wait...");

                Table table = dynamoDB.createTable(
                        tableName,
                        Arrays.asList(
                                new KeySchemaElement("email", KeyType.HASH) // Partition key
                        ),
                        Arrays.asList(
                                new AttributeDefinition("email", ScalarAttributeType.S)
                        ),
                        new ProvisionedThroughput(10L, 10L)
                );

                table.waitForActive();
                System.out.println("Success. Table status: " + table.getDescription().getTableStatus());

            } catch (InterruptedException ie) {
                System.err.println("Unable to create table: ");
                System.err.println(ie.getMessage());
            }
        }

        Table table = dynamoDB.getTable(tableName);

        System.out.println("Loading users into login table...");


        table.putItem(new Item()
                .withPrimaryKey("email", "s40937880@student.rmit.edu.au")
                .withString("user_name", "Stephen Oberoi0")
                .withString("password", "012345"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s41031851@student.rmit.edu.au")
                .withString("user_name", "Gokul Krissna1")
                .withString("password", "123456"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s41738972@student.rmit.edu.au")
                .withString("user_name", "Ekansh Sharma2")
                .withString("password", "234567"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s40892503@student.rmit.edu.au")
                .withString("user_name", "Jaskaran Singh3")
                .withString("password", "345678"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s12345674@student.rmit.edu.au")
                .withString("user_name", "EmmaThompson4")
                .withString("password", "456789"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s23456785@student.rmit.edu.au")
                .withString("user_name", "NoahJohnson5")
                .withString("password", "567890"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s34567896@student.rmit.edu.au")
                .withString("user_name", "AvaWhite6")
                .withString("password", "678901"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s45678907@student.rmit.edu.au")
                .withString("user_name", "EthanClark7")
                .withString("password", "789012"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s56789018@student.rmit.edu.au")
                .withString("user_name", "IsabellaLee8")
                .withString("password", "890123"));

        table.putItem(new Item()
                .withPrimaryKey("email", "s67890129@student.rmit.edu.au")
                .withString("user_name", "MasonHall9")
                .withString("password", "901234"));

        System.out.println("Done! All 10 users loaded into the login table.");
    }
}