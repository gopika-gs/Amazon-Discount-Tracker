package testpkg;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pagepkg.Pagetest;

import java.util.List;

public class AmazonTest {

    WebDriver driver;
    Pagetest page;
    String baseUrl = "https://www.amazon.in/";

    @BeforeMethod
    public void setup() {
        // Initialize WebDriver
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(baseUrl);

        // Initialize Page Object
        page = new Pagetest(driver);
    }

    @Test
    public void test() {
        List<String> searchTerms = List.of("Oven", "Microwave", "Toaster");
        StringBuilder finalHtmlContent = new StringBuilder();

        // Start HTML document
        finalHtmlContent.append("<html><head><title>Amazon Search Results</title></head><body>");
        finalHtmlContent.append("<h1>Amazon Search Results</h1>");

        for (String searchTerm : searchTerms) {
            try {
                // Search and process products for each search term
                List<Pagetest.Product> productList = page.searchAndProcessProducts(searchTerm);

                // Generate and accumulate the HTML content for each search term
                finalHtmlContent.append(page.generateHtmlReport(searchTerm, productList));

                System.out.println("Successfully processed products for: " + searchTerm);
            } catch (Exception e) {
                System.err.println("Failed to process products for: " + searchTerm + " due to " + e.getMessage());
            }
        }

        // End HTML document
        finalHtmlContent.append("</body></html>");

        // Write the accumulated HTML content to a single file
        page.writeToFile("amazon_discounts.html", finalHtmlContent.toString());
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
