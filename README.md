# 🩺 Cochrane Data Scraper

A Java-based Selenium web scraper that extracts medical review data from the [Cochrane Library](https://www.cochranelibrary.com/). This tool automates searching for topics, scraping relevant reviews, and exporting them to a CSV file for further analysis.

## 📦 Project Structure

```
cochrane-crawler/
├── pom.xml                             # Maven configuration file
├── cochrane_reviews_test.csv          # Sample output (scraped reviews)
├── target/
│   ├── cochrane-crawler-1.0-SNAPSHOT.jar  # Compiled executable JAR
│   └── ... (build output files)
├── src/
│   └── main/java/com/vantage/
│       └── App.java                   # Main Java application
└── Cochrane Library Scraping Solution Documentation.pdf  # Project report
```

## ⚙️ Technologies Used

- **Java 17+**
- **Selenium WebDriver** – for browser automation and scraping
- **Maven** – for project management and dependency handling
- **JUnit** – for unit testing

## 🚀 How to Run the Project

### ✅ Prerequisites

- Java 17 or newer installed
- [Apache Maven](https://maven.apache.org/) installed
- [Google Chrome](https://www.google.com/chrome/) installed
- [ChromeDriver](https://chromedriver.chromium.org/downloads) (compatible with your Chrome version) in your system PATH

### 🛠️ Setup & Execution

1. **Clone the Repository**

```bash
git clone git@github.com:emad-8869/Cochrane-Data-Scraper.git
cd Cochrane-Data-Scraper
```

2. **Build the Project**

```bash
mvn clean package
```

This will generate `cochrane-crawler-1.0-SNAPSHOT.jar` inside the `target/` directory.

3. **Run the Scraper**

```bash
mvn exec:java -Dexec.mainClass="com.vantage.App"
```

4. **Output**

The scraped review data will be saved in `cochrane_reviews_test.csv`.

## 🧪 Running Tests

Run unit tests using:

```bash
mvn test
```

## 👤 Author

**Emad Hassan**  
[GitHub Profile](https://github.com/emad-8869)
"""
