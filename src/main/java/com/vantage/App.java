package com.vantage;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class App {

    private static String removeNonAscii(String str) {
        return str.replaceAll("[^\\x00-\\x7F]+", "");
    }

    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "/opt/homebrew/bin/chromedriver");

        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60)); // Increased timeout

        String baseUrl = "https://www.cochranelibrary.com"; // Include the base URL
        String topicsUrl = "https://www.cochranelibrary.com/cdsr/reviews/topics";
        String outputFile = "cochrane_reviews_test.csv"; // Define the output file name

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
             PrintWriter printWriter = new PrintWriter(outputStreamWriter)) {

            // Write CSV header
            printWriter.println("URL|Topic|Title|Authors|Date");
            printWriter.flush();

            System.out.println("🌐 Navigated to Cochrane Topics Page");
            driver.get(topicsUrl);

            // Wait for topic list to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.browse-by-term-list li.browse-by-list-item a")));

            List<WebElement> topicLinks = driver.findElements(By.cssSelector("ul.browse-by-term-list li.browse-by-list-item a"));
            System.out.println("✅ Found " + topicLinks.size() + " topic links");

            if (topicLinks.isEmpty()) {
                System.out.println("❌ No topic links found!");
                return; // Exit if no topics are found
            }

            // Process all topics
            for (int i = 0; i < topicLinks.size(); i++) {
                WebElement topicLink = topicLinks.get(i);
                String topicName = topicLink.getText().trim();
                String topicUrl = topicLink.getAttribute("href");

                System.out.println("🔍 Visiting topic: " + topicName + " | " + topicUrl);

                // Open the topic in a new window
                String currentWindowHandle = driver.getWindowHandle();
                ((JavascriptExecutor) driver).executeScript("window.open('" + topicUrl + "', '_blank');");

                // Switch to the new window
                Set<String> windowHandles = driver.getWindowHandles();
                for (String handle : windowHandles) {
                    if (!handle.equals(currentWindowHandle)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }

                // Wait for review list to load dynamically
                try {
                    WebElement reviewTitle = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1, .review-title, .result-title")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", reviewTitle); // Scroll the element into view
                } catch (TimeoutException e) {
                    System.out.println("⚠️ Timeout waiting for review title on " + topicUrl);
                    continue;
                }

                // Fetch reviews dynamically
                List<WebElement> reviews = driver.findElements(By.cssSelector("div.search-results-item"));
                System.out.println("🔎 Found " + reviews.size() + " reviews");

                if (reviews.isEmpty()) {
                    System.out.println("⚠️ No reviews found for topic: " + topicName);
                    continue; // Skip to the next topic if no reviews are found
                }

                // Process each review
                for (int j = 0; j < reviews.size(); j++) {
                    try {
                        WebElement review = reviews.get(j);
                        System.out.println("🔎 Processing review #" + (j + 1));

                        String title = "No title";
                        String reviewUrl = "No URL";
                        String authors = "No authors";
                        String date = "No date";

                        try {
                            WebElement titleEl = review.findElement(By.cssSelector("h3.result-title a"));
                            title = titleEl.getText().trim();
                            reviewUrl = titleEl.getAttribute("href");

                            // Check if the URL is relative and prepend the base URL if necessary
                            if (!reviewUrl.startsWith("http")) {
                                reviewUrl = baseUrl + reviewUrl;
                            }

                            // Validate URL
                            if (reviewUrl.isEmpty() || !reviewUrl.startsWith("http")) {
                                reviewUrl = "Invalid URL";
                            }

                            reviewUrl = reviewUrl.trim(); // Remove leading/trailing whitespace
                            reviewUrl = removeNonAscii(reviewUrl); // Remove non-ASCII characters

                            System.out.println("Extracted URL: " + reviewUrl); // Debugging: Print the extracted URL
                        } catch (NoSuchElementException e) {
                            System.out.println("⚠️ No title or URL found for review #" + (j + 1));
                        }

                        // Extract authors (revised)
                        try {
                            List<WebElement> authorElements = review.findElements(By.cssSelector("div.search-results-item div.search-result-authors, span.author, ul.authors, div.meta-info span:nth-child(1)"));
                            if (!authorElements.isEmpty()) {
                                StringBuilder authorText = new StringBuilder();
                                for (WebElement authorEl : authorElements) {
                                    authorText.append(authorEl.getText().trim()).append(", ");
                                }
                                authors = authorText.substring(0, authorText.length() - 2); // Remove trailing comma and space
                            } else {
                                authors = "No authors found";
                            }
                        } catch (NoSuchElementException e) {
                            authors = "No authors found";
                            System.err.println("⚠️ Could not find authors: " + e.getMessage());
                        }

                        // Extract date (revised)
                        try {
                            WebElement dateElement = review.findElement(By.cssSelector("div.search-results-item div.search-result-date, span.date, time, meta[itemprop='datePublished'], div.meta-info span:nth-child(2)"));
                            date = dateElement.getText().trim();
                        } catch (NoSuchElementException e) {
                            date = "No date found";
                            System.err.println("⚠️ Could not find date: " + e.getMessage());
                        }

                        // Print the output in terminal with pipe delimiter
                        System.out.printf("✅ Wrote line to file: %s|%s|%s|%s|%s%n",
                                         reviewUrl,
                                         topicName,
                                         title,
                                         authors,
                                         date);

                        // Write directly to the file with pipe delimiter
                        String line = String.format("%s|%s|%s|%s|%s\n",
                                             reviewUrl,
                                             topicName,
                                             title,
                                             authors,
                                             date);
                        printWriter.write(line);
                        printWriter.flush(); // Flush after each write
                    } catch (Exception e) {
                        System.err.println("🔎 Unable to process review #" + (j + 1) + " due to missing elements or page layout changes.");
                        System.err.println("⚠️ Skipped a review in topic: " + topicName + " due to error: " + e.getMessage());
                    }
                }

                // Close the new window after processing the topic
                driver.close();

                // Switch back to the original window
                driver.switchTo().window(currentWindowHandle);
            }

            System.out.println("✅ Saved all reviews to " + outputFile);
        } catch (Exception e) {
            System.err.println("❌ Error occurred");
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}