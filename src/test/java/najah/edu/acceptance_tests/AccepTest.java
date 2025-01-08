package najah.edu.acceptance_tests;
import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/My_features", // Path to feature files
        plugin = {
                "pretty", // Pretty print output
                "html:target/cucumber-reports/cucumber.html", // HTML report
                "json:target/cucumber-reports/cucumber.json" // JSON report
        },
        monochrome = true, // Make console output readable
        snippets = CucumberOptions.SnippetType.CAMELCASE, // Use camelCase for step definitions
        glue = "najah.edu.step_definitions" // Path to step definitions
)

public class AccepTest {

}
