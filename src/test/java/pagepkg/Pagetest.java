package pagepkg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Pagetest {

    WebDriver driver;
    WebDriverWait wait;

    @FindBy(xpath = "//*[@placeholder=\"Search Amazon.in\"]")
    WebElement searchbox;

    public Pagetest(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }



    public List<Product> searchAndProcessProducts(String searchTerm) {
        wait.until(ExpectedConditions.visibilityOf(searchbox));
        searchbox.clear();
        searchbox.sendKeys(searchTerm);
        searchbox.submit();

        boolean hasNextPage = true;
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><title>Amazon Product Discounts</title></head><body>");
        htmlContent.append("<h1>Amazon Products with Discounts Greater Than 50%</h1>");

        List<Product> productList = new ArrayList<>();
        while (hasNextPage) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));

            List<WebElement> products = driver.findElements(By.cssSelector("div.s-main-slot div.s-result-item"));
            System.out.println("Total Products Found: " + products.size());

            for (WebElement product : products) {
                try {
                    List<WebElement> discountPriceElements = product.findElements(By.xpath(".//span[contains(text(), '% off')]"));

                    for (WebElement discountPriceElement : discountPriceElements) {
                        String discountText = discountPriceElement.getText();
                        System.out.println("Raw Discount Text: " + discountText);

                        int discountPercentage = extractDiscountPercentage(discountText);
                        System.out.println("Parsed Discount Percentage: " + discountPercentage);

                        if (discountPercentage > 50) {
                            WebElement titleElement = product.findElement(By.xpath(".//span[contains(@class, 'a-text-normal')]"));
                            String productTitle = titleElement.getText();

                            WebElement linkElement = product.findElement(By.cssSelector("a.a-link-normal"));
                            String productLink = linkElement.getAttribute("href");

                            WebElement imageElement = product.findElement(By.xpath(".//img[contains(@class, 's-image')]"));
                            String imageUrl = imageElement != null ? imageElement.getAttribute("src") : "Image not found";

                            WebElement priceElement = product.findElement(By.xpath(".//span[@class='a-price-whole']"));
                            String price = priceElement != null ? priceElement.getText() : "Price not found";

                            WebElement mrpElement = product.findElement(By.xpath(".//span[@data-a-color=\"secondary\"]"));
                            String mrp = mrpElement != null ? mrpElement.getText() : "MRP not found";

                            // Add product details to the list
                            productList.add(new Product(productTitle, productLink, discountPercentage, imageUrl, price, mrp));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing product: " + e.getMessage());
                }
            }

            // Check for the next page button and navigate if available
            try {
                WebElement nextPageButton = driver.findElement(By.xpath("//li[@class='a-last']//a"));
                if (nextPageButton.isDisplayed()) {
                    nextPageButton.click();
                    wait.until(ExpectedConditions.stalenessOf(products.get(products.size() - 1)));
                } else {
                    hasNextPage = false;
                }
            } catch (Exception e) {
                hasNextPage = false;
            }
        }

        htmlContent.append("</body></html>");
        writeToFile("amazon_discounts.html", htmlContent.toString());

        // Sort products by discount percentage in descending order
        Collections.sort(productList, Comparator.comparingInt(Product::getDiscountPercentage).reversed());

        return productList;
    }

    // Helper method to extract discount percentage from text
    private int extractDiscountPercentage(String discountText) {
        try {
            String cleanedText = discountText.replace("(", "")
                    .replace(")", "")
                    .replace("% off", "")
                    .trim();
            return Integer.parseInt(cleanedText);
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse discount percentage from text: " + discountText);
            return 0; // Return 0 if parsing fails
        }
    }

    // Method to write HTML content to a file
    public void writeToFile(String fileName, String htmlContent) {
        // Define the file path to save in the project directory
        String filePath = System.getProperty("user.dir") + "/" + fileName;
        System.out.println("File path: " + filePath);

        // Write the HTML content to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(htmlContent);
            System.out.println("File saved at: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save the HTML file: " + e.getMessage());
        }
    }

    // Method to generate the HTML report
    public String generateHtmlReport(String searchTerm, List<Product> productList) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head><title>").append(searchTerm).append(" Product Discounts</title>");
        htmlContent.append("<style>");
        htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; }");
        htmlContent.append(".container { width: 80%; margin: 0 auto; background-color: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }");
        htmlContent.append("h1 { text-align: center; color: #333; }");
        htmlContent.append(".product { margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #ddd; }");
        htmlContent.append(".product img { float: left; margin-right: 20px; width: 200px; height: 200px; object-fit: cover; border-radius: 10px; }");
        htmlContent.append(".product h2 { margin: 0; color: #555; }");
        htmlContent.append(".product p { margin: 5px 0; color: #777; }");
        htmlContent.append(".product a { display: inline-block; margin-top: 10px; text-decoration: none; color: #fff; background-color: #28a745; padding: 10px 15px; border-radius: 5px; }");
        htmlContent.append(".product a:hover { background-color: #218838; }");
        htmlContent.append(".clearfix { clear: both; }");
        htmlContent.append("</style>");
        htmlContent.append("</head><body>");
        htmlContent.append("<div class='container'>");
        htmlContent.append("<h1>Amazon Products with Discounts Greater Than 50% for '").append(searchTerm).append("'</h1>");

        htmlContent.append("<h2>").append(searchTerm).append("</h2>");
        for (Product product : productList) {
            htmlContent.append("<div class='product'>");
            htmlContent.append("<img src=\"").append(product.getImageUrl()).append("\" alt=\"Product Image\"/>");
            htmlContent.append("<h2>").append(product.getTitle()).append("</h2>");
            htmlContent.append("<p>Price: ₹").append(product.getPrice()).append("</p>");
            htmlContent.append("<p>MRP: <span style=\"text-decoration: line-through; color: grey;\">").append(product.getMrp()).append("</span></p>");
            htmlContent.append("<p>Discount: ").append(product.getDiscountPercentage()).append("% off</p>");
            htmlContent.append("<a href=\"").append(product.getLink()).append("\" target=\"_blank\">View Product</a>");
            htmlContent.append("<div class='clearfix'></div>");
            htmlContent.append("</div>");
        }

        htmlContent.append("</div>");
        htmlContent.append("</body></html>");
        return htmlContent.toString();
    }

    // Product class to store product details
    public static class Product {
        private String title;
        private String link;
        private int discountPercentage;
        private String imageUrl;
        private String price;
        private String mrp;

        public Product(String title, String link, int discountPercentage, String imageUrl, String price, String mrp) {
            this.title = title;
            this.link = link;
            this.discountPercentage = discountPercentage;
            this.imageUrl = imageUrl;
            this.price = price;
            this.mrp = mrp;
        }

        public String getTitle() {
            return title;
        }

        public String getLink() {
            return link;
        }

        public int getDiscountPercentage() {
            return discountPercentage;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getPrice() {
            return price;
        }

        public String getMrp() {
            return mrp;
        }
    }
}
