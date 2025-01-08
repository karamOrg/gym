package najah.edu.acceptance_tests;



import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.example.fitness.ClientDashboard;
import com.example.fitness.Main;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientDashboardTest {

    @TempDir
    static Path tempDir;

    private static File usersFile;
    private static File articlesFile;

    @BeforeAll
    static void setupAll() {
        usersFile = tempDir.resolve("users.txt").toFile();
        articlesFile = tempDir.resolve("articles.txt").toFile();
        Main.USERS_FILE = usersFile.getAbsolutePath();
        Main.ARTICLES_FILE = articlesFile.getAbsolutePath();
    }

    @BeforeEach
    void setupEach() throws IOException {
       
        if (!usersFile.exists()) {
            assertTrue(usersFile.createNewFile(), "Users file should be created successfully");
        } else {
            new PrintWriter(usersFile).close(); // Clear contents
        }

       
        if (!articlesFile.exists()) {
            assertTrue(articlesFile.createNewFile(), "Articles file should be created successfully");
        } else {
            new PrintWriter(articlesFile).close(); // Clear contents
        }
    }

    @Test
    @Order(1)
    @DisplayName("showDashboard(): choose 7 => logout immediately")
    void testShowDashboard_exit() {
        String input = "7\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.showDashboard("testClient");

        System.setOut(System.out);
        System.setIn(System.in);

        String consoleOutput = outStream.toString();
        assertTrue(
            consoleOutput.contains("Logging out... Goodbye, testClient!"),
            "Should show logout message with username"
        );
    }

    @Test
    @Order(2)
    @DisplayName("showDashboard(): invalid numeric => then exit")
    void testShowDashboard_invalidOption() {
        String input = "99\n7\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.showDashboard("clientUser");

        System.setOut(System.out);
        System.setIn(System.in);

        String consoleOutput = outStream.toString();
        assertTrue(
            consoleOutput.contains("Invalid option! Please select a valid option."),
            "Should warn about invalid menu choice"
        );
        assertTrue(
            consoleOutput.contains("Logging out... Goodbye, clientUser!"),
            "Should eventually log out"
        );
    }

    @Test
    @Order(3)
    @DisplayName("showDashboard(): non-numeric => then exit")
    void testShowDashboard_nonNumeric() {
        String input = "abc\n7\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.showDashboard("clientUser");

        System.setOut(System.out);
        System.setIn(System.in);

        String consoleOutput = outStream.toString();
        assertTrue(
            consoleOutput.contains("Invalid input! Please enter a number."),
            "Should show invalid input message for non-numeric"
        );
        assertTrue(
            consoleOutput.contains("Logging out... Goodbye, clientUser!"),
            "Should eventually log out"
        );
    }

    @Test
    @Order(4)
    @DisplayName("viewProfile(): no users => profile not found")
    void testViewProfile_noData() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.viewProfile("johnDoe");

        System.setOut(System.out);
        String consoleOutput = outStream.toString();
        assertTrue(
            consoleOutput.contains("No profile found for username: johnDoe"),
            "Should indicate no profile found"
        );
    }

    @Test
    @Order(5)
    @DisplayName("viewProfile(): user found => prints profile")
    void testViewProfile_found() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(usersFile))) {
            bw.write("johnDoe,hash,client,male,30,active,premium\n");
        }

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.viewProfile("johnDoe");

        System.setOut(System.out);
        String output = outStream.toString();
        assertTrue(output.contains("Profile Details:"), "Should display 'Profile Details'");
        assertTrue(output.contains("Username: johnDoe"), "Should show username 'johnDoe'");
        assertTrue(output.contains("Role: client"), "Should show role 'client'");
        assertTrue(output.contains("Gender: male"), "Should show gender 'male'");
        assertTrue(output.contains("Age: 30"), "Should show age '30'");
        assertTrue(output.contains("Status: active"), "Should show status 'active'");
        assertTrue(output.contains("Subscription: premium"), "Should show subscription 'premium'");
    }

    @Test
    @Order(6)
    @DisplayName("readArticles(): no articles => warns user")
    void testReadArticles_none() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.readArticles();

        System.setOut(System.out);
        String output = outStream.toString();
        assertTrue(
            output.contains("No articles available at the moment.")
                || output.contains("No articles file found."),
            "Should warn user that no articles are available"
        );
    }

    @Test
    @Order(7)
    @DisplayName("readArticles(): some articles => pick an ID => show details")
    void testReadArticles_some() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(articlesFile))) {
            bw.write("A111,Title One,authorA,2025-01-01,Some content\n");
            bw.write("A222,Title Two,authorB,2025-02-02,Other content\n");
        }

        String input = "A111\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.readArticles();

        System.setOut(System.out);
        System.setIn(System.in);

        String output = outStream.toString();
        assertTrue(output.contains("ID: A111 - Title: Title One"), "Should list A111");
        assertTrue(output.contains("ID: A222 - Title: Title Two"), "Should list A222");
        assertTrue(output.contains("---- Article Details ----"), "Should show the details header");
        assertTrue(output.contains("Title: Title One"), "Should show 'Title One'");
        assertTrue(output.contains("Publish Date: 2025-01-01"), "Should show '2025-01-01'");
        assertTrue(output.contains("Content: Some content"), "Should show content 'Some content'");
    }

    @Test
    @Order(8)
    @DisplayName("changeSubscription(): user not found => no subscription changed")
    void testChangeSubscription_noUser() {
        String input = "3\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ClientDashboard.changeSubscription("missingUser");

        System.setOut(System.out);
        System.setIn(System.in);

        String output = outStream.toString();
        assertTrue(
            output.contains("No subscription found or user not found. Cannot change subscription."),
            "Should warn that user not found"
        );
    }


}
