package com.example;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class ScreenshotTool {
    public static void main(String[] args) throws Exception {
        Path csvPath = Paths.get(System.getProperty("user.home"), "Documents", "Magic_Tool", "URLs.csv");
        Path templatePath = Paths.get("template", "ReportTemplate.docx");
        Path outputDir = Paths.get(System.getProperty("user.home"), "Documents", "Magic_Tool", LocalDate.now().toString());
        Files.createDirectories(outputDir);

        List<String> urls = Files.readAllLines(csvPath).stream()
                .map(String::trim).filter(s -> !s.isEmpty()).toList();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--window-size=1920,1080");
        WebDriver driver = new ChromeDriver(options);

        Map<String, String> placeholderToUrl = new HashMap<>();
        Map<String, String> placeholderToImgPath = new HashMap<>();

        int count = 1;
        for (String url : urls) {
            String urlKey = "$URL" + count;
            String imgKey = "$IMG" + count;
            placeholderToUrl.put(urlKey, url);

            driver.get(url);
            Thread.sleep(2000);
            String imgFile = "screenshot_" + count + ".png";
            Path screenshotPath = outputDir.resolve(imgFile);
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(srcFile.toPath(), screenshotPath, StandardCopyOption.REPLACE_EXISTING);
            placeholderToImgPath.put(imgKey, screenshotPath.toString());

            count++;
        }

        driver.quit();

        try (FileInputStream fis = new FileInputStream(templatePath.toFile());
             XWPFDocument doc = new XWPFDocument(fis)) {

            for (XWPFParagraph para : doc.getParagraphs()) {
                replacePlaceholdersInParagraph(para, placeholderToUrl, placeholderToImgPath, doc);
            }

            Path outputDoc = outputDir.resolve("Report_" + LocalDate.now() + ".docx");
            try (FileOutputStream out = new FileOutputStream(outputDoc.toFile())) {
                doc.write(out);
            }

            System.out.println("✅ Report saved: " + outputDoc);
        }
    }

    private static void replacePlaceholdersInParagraph(XWPFParagraph para,
                                                       Map<String, String> urlMap,
                                                       Map<String, String> imgMap,
                                                       XWPFDocument doc) {
        String text = para.getText();
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            if (text.contains(entry.getKey())) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }

        // Replace paragraph text first
        if (!text.equals(para.getText())) {
            for (int i = para.getRuns().size() - 1; i >= 0; i--) {
                para.removeRun(i);
            }
            para.createRun().setText(text);
        }

        // Insert images
        for (Map.Entry<String, String> entry : imgMap.entrySet()) {
            if (text.contains(entry.getKey())) {
                XWPFParagraph newPara = doc.insertNewParagraph(para.getCTP().newCursor());
                XWPFRun run = newPara.createRun();
                try (FileInputStream is = new FileInputStream(entry.getValue())) {
                    run.addPicture(is,
                            Document.PICTURE_TYPE_PNG,
                            entry.getValue(),
                            Units.toEMU(500),
                            Units.toEMU(300));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
