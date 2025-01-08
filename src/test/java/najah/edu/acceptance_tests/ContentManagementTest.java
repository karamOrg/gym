package najah.edu.acceptance_tests;


import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import com.example.fitness.ContentManagement;
import com.example.fitness.Main;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContentManagementTest {

    @TempDir
    static Path tempDir;

    private static File articlesFile;

    @BeforeAll
    static void setupAll() {
        // Create a reference to "articles.txt" in a temp directory
        articlesFile = tempDir.resolve("articles.txt").toFile();
        // Point Main.ARTICLES_FILE to that file
        Main.ARTICLES_FILE = articlesFile.getAbsolutePath();
    }

    @BeforeEach
    void clearFile() throws IOException {
        // Ensure the file is empty for each test
        if (!articlesFile.exists()) {
            articlesFile.createNewFile();
        } else {
            new PrintWriter(articlesFile).close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("manageContent(): choose 5 => exit immediately")
    void testManageContent_exit() {
        // "5" means "Back to Dashboard"
        String input = "5\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ContentManagement.manageContent("testUser");

        System.setOut(System.out);
        System.setIn(System.in);

        String output = outStream.toString();
        // Should see "Returning to Dashboard..." somewhere
        assertTrue(output.contains("Returning to Dashboard..."),
                   "Should print 'Returning to Dashboard...' after choosing 5");
    }

    // -------------------------------------------------------------
    // 2) manageContent(...) => invalid numeric => then choose 5 => exit
    // -------------------------------------------------------------
    @Test
    @Order(2)
    @DisplayName("manageContent(): invalid numeric => then exit with 5")
    void testManageContent_invalidOption() {
        String input = "99\n5\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ContentManagement.manageContent("tester");

        System.setOut(System.out);
        System.setIn(System.in);

        String output = outStream.toString();
        assertTrue(output.contains("Invalid option. Please select a valid option."),
                   "Should show invalid option message");
        assertTrue(output.contains("Returning to Dashboard..."),
                   "Should eventually exit with choice 5");
    }

    // -------------------------------------------------------------
    // 3) printAllArticles() => empty => "No articles available."
    // -------------------------------------------------------------
    @Test
    @Order(3)
    @DisplayName("printAllArticles(): none => warns user")
    void testPrintAllArticles_none() {
        // No articles in the file => direct call
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ContentManagement.printAllArticles();

        System.setOut(System.out);
        String output = outStream.toString();
        assertTrue(output.contains("No articles available."),
                   "Should indicate no articles available");
    }

    // -------------------------------------------------------------
    // 4) addNewArticle(...) => success => file has new article
    // -------------------------------------------------------------
    @Test
    @Order(4)
    @DisplayName("addNewArticle(): success => appends to file")
    void testAddNewArticle_success() throws IOException {
        // Provide title, then content
        String input = "My Title\n" +
                       "Some content body\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        // If your code calls addNewArticle("testAuthor")
        ContentManagement.addNewArticle("testAuthor");

        System.setOut(System.out);
        System.setIn(System.in);

        String output = outStream.toString();
        // Expect something like "Article created successfully with ID: 12345"
        assertTrue(output.contains("Article created successfully with ID:"),
                   "Should confirm article creation");

        // Now check file contents
        try (BufferedReader br = new BufferedReader(new FileReader(articlesFile))) {
            String line = br.readLine();
            assertNotNull(line, "Should have 1 line in file");
            String[] parts = line.split(",", 5);
            assertEquals("My Title", parts[1], "Title should match input");
            assertEquals("testAuthor", parts[2], "Author should match 'testAuthor'");
            // parts[3] is publishDate; parts[4] is content => "Some content body"
            assertEquals("Some content body", parts[4], "Should match article content");
        }
    }

    // -------------------------------------------------------------
    // 5) editArticleById() => ID not found => warns user
    // -------------------------------------------------------------
    @Test
    @Order(5)
    @DisplayName("editArticleById(): article not found => warns user")
    void testEditArticleById_notFound() {
        // Provide an ID that doesn't exist => "999" => new content => "blah"
        String input = "999\nblah\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ContentManagement.editArticleById();

        System.setOut(System.out);
        System.setIn(System.in);

        String output = outStream.toString();
        assertTrue(output.contains("Article ID 999 not found."),
                   "Should warn about missing article ID");
    }

    // -------------------------------------------------------------
    // 6) editArticleById() => success
    // -------------------------------------------------------------
    @Test
    @Order(6)
    @DisplayName("editArticleById(): existing => updates content")
    void testEditArticleById_success() throws IOException {
        // We'll add an article with ID "12345"
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(articlesFile))) {
            bw.write("12345,TitleOne,authorX,2025-01-01,Old content");
            bw.newLine();
        }

        // Provide ID=12345, new content= "New edited content"
        String input = "12345\nNew edited content\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ContentManagement.editArticleById();

        System.setOut(System.out);
        System.setIn(System.in);

        String consoleOutput = outStream.toString();
        assertTrue(consoleOutput.contains("Article ID 12345 updated!"),
                   "Should confirm article updated");
        assertTrue(consoleOutput.contains("New Content: New edited content"),
                   "Should show new content in output");

        // Check the file => "12345,TitleOne,authorX,2025-01-01,New edited content"
        try (BufferedReader br = new BufferedReader(new FileReader(articlesFile))) {
            String line = br.readLine();
            String[] parts = line.split(",", 5);
            assertEquals("12345", parts[0], "ID stays same");
            assertEquals("TitleOne", parts[1], "Title stays same");
            assertEquals("authorX", parts[2], "Author stays same");
            assertEquals("2025-01-01", parts[3], "Date stays same");
            assertEquals("New edited content", parts[4], "Content is updated");
        }
    }

    // -------------------------------------------------------------
    // 7) deleteArticleById() => not found => warns user
    // -------------------------------------------------------------
    @Test
    @Order(7)
    @DisplayName("deleteArticleById(): no matching ID => warns user")
    void testDeleteArticleById_notFound() throws IOException {
        // Put some article with ID= "ABCDE"
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(articlesFile))) {
            bw.write("ABCDE,TitleQ,authorQ,2025-01-01,Hello world");
            bw.newLine();
        }

        // We'll attempt to delete "XYZ999"
        String input = "XYZ999\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ContentManagement.deleteArticleById();

        System.setOut(System.out);
        System.setIn(System.in);

        String consoleOutput = outStream.toString();
        assertTrue(consoleOutput.contains("Article ID XYZ999 not found."),
                   "Should warn about missing ID");
        
        // Check that file content is unchanged
        try (BufferedReader br = new BufferedReader(new FileReader(articlesFile))) {
            String line = br.readLine();
            assertNotNull(line);
            assertTrue(line.startsWith("ABCDE,"), "Article ABCDE remains in the file");
        }
    }

    // -------------------------------------------------------------
    // 8) deleteArticleById() => success
    // -------------------------------------------------------------
    @Test
    @Order(8)
    @DisplayName("deleteArticleById(): existing => removed from file")
    void testDeleteArticleById_success() throws IOException {
        // We'll store two articles, we'll remove the first one
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(articlesFile))) {
            bw.write("11111,TitleA,AuthA,2025-01-01,ContentA\n");
            bw.write("22222,TitleB,AuthB,2025-02-02,ContentB\n");
        }

        // We'll delete ID= "11111"
        String input = "11111\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        ContentManagement.deleteArticleById();

        System.setOut(System.out);
        System.setIn(System.in);

        String consoleOutput = outStream.toString();
        assertTrue(consoleOutput.contains("Article ID 11111 deleted successfully!"),
                   "Should confirm article is deleted");

        // Now the file should only contain "22222..."
        try (BufferedReader br = new BufferedReader(new FileReader(articlesFile))) {
            String remaining = br.readLine();
            assertNotNull(remaining, "There should be one remaining article line");
            assertTrue(remaining.startsWith("22222,"), "Should keep only the second article");
            assertNull(br.readLine(), "No more lines after that");
        }
    }
    
    
}
