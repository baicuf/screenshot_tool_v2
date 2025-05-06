package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ScreenshotTool {
    public static void main(String[] args) throws Exception {
        String userHome = System.getProperty("user.home");
        Path csvPath = Paths.get(userHome, "Documents", "Magic_Tool", "URLs.csv");

        if (!Files.exists(csvPath)) {
            System.err.println("CSV file not found: " + csvPath);
            return;
        }

        List<String> urls = Files.readAllLines(csvPath).stream()
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(Collectors.toList());

        String folderName = LocalDate.now().toString();
        Path outputDir = Paths.get(userHome, "Documents", "Magic_Tool", folderName);
        Files.createDirectories(outputDir);

        Path driverPath = Paths.get("drivers", "chromedriver.exe");
        if (!Files.exists(driverPath)) {
            System.err.println("chromedriver.exe not found in 'drivers/' folder.");
            return;
        }

        System.setProperty("webdriver.chrome.driver", driverPath.toAbsolutePath().toString());
        WebDriver driver = new ChromeDriver();

        int count = 1;
        for (String url : urls) {
            try {
                driver.get(url);
                Thread.sleep(2000);

                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                Path dest = outputDir.resolve("screenshot_" + count + ".png");
                Files.copy(screenshot.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("✔️ Saved: " + dest.toString());
                count++;
            } catch (Exception e) {
                System.err.println("❌ Error for URL: " + url);
                e.printStackTrace();
            }
        }

        driver.quit();
    }
}
