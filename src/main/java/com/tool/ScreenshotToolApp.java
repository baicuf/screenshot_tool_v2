package com.tool;

import org.apache.poi.xwpf.usermodel.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ScreenshotToolApp {
    private static WebDriver driver;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ScreenshotToolApp::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Magic Screenshot Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setLayout(new GridLayout(5, 1));

        JButton selectCSV = new JButton("Select URLs.csv");
        JButton selectFolder = new JButton("Select Output Folder");
        JButton selectTemplate = new JButton("Select DOCX Template");
        JButton startButton = new JButton("Start");
        JLabel status = new JLabel("Waiting...", SwingConstants.CENTER);

        final Path[] csvPath = new Path[1];
        final Path[] outputDir = new Path[1];
        final Path[] docxTemplate = new Path[1];

        selectCSV.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                csvPath[0] = fc.getSelectedFile().toPath();
                status.setText("CSV selected: " + csvPath[0].getFileName());
            }
        });

        selectFolder.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                outputDir[0] = fc.getSelectedFile().toPath();
                status.setText("Output folder selected: " + outputDir[0].getFileName());
            }
        });

        selectTemplate.addActionListener((ActionEvent e) -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                docxTemplate[0] = fc.getSelectedFile().toPath();
                status.setText("Template selected: " + docxTemplate[0].getFileName());
            }
        });

        startButton.addActionListener((ActionEvent e) -> {
            if (csvPath[0] == null || outputDir[0] == null || docxTemplate[0] == null) {
                JOptionPane.showMessageDialog(frame, "Please select all required files.");
                return;
            }
            try {
                run(csvPath[0], outputDir[0], docxTemplate[0]);
                status.setText("Done!");
            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Error: " + ex.getMessage());
            }
        });

        frame.add(selectCSV);
        frame.add(selectFolder);
        frame.add(selectTemplate);
        frame.add(startButton);
        frame.add(status);
        frame.setVisible(true);
    }

    private static void run(Path csv, Path output, Path template) throws Exception {
        List<String> urls = Files.readAllLines(csv).stream()
                .map(String::trim).filter(line -> !line.isEmpty()).collect(Collectors.toList());

        Path chromeDriver = Paths.get("drivers", "chromedriver.exe");
        if (!Files.exists(chromeDriver)) {
            throw new FileNotFoundException("chromedriver.exe not found in 'drivers/'");
        }
        System.setProperty("webdriver.chrome.driver", chromeDriver.toAbsolutePath().toString());

        driver = new ChromeDriver();
        JOptionPane.showMessageDialog(null, "Log in if needed. Then press OK to begin capturing.");

        Path datedOutput = output.resolve(LocalDate.now().toString());
        Files.createDirectories(datedOutput);

        for (int i = 0; i < urls.size(); i++) {
            String url = urls.get(i);
            driver.get(url);
            Thread.sleep(2000);

            Screenshot sc = new AShot().shootingAll().takeScreenshot(driver);
            File imageFile = datedOutput.resolve("screenshot_" + (i + 1) + ".png").toFile();
            ImageIO.write(sc.getImage(), "PNG", imageFile);

            try (InputStream docStream = Files.newInputStream(template);
                 XWPFDocument doc = new XWPFDocument(docStream)) {

                for (XWPFParagraph p : doc.getParagraphs()) {
                    for (XWPFRun run : p.getRuns()) {
                        String txt = run.getText(0);
                        if (txt != null && txt.contains("${URL}")) {
                            run.setText(txt.replace("${URL}", url), 0);
                        }
                    }
                }

                XWPFParagraph imgPara = doc.createParagraph();
                XWPFRun imgRun = imgPara.createRun();
                imgRun.addPicture(new FileInputStream(imageFile), Document.PICTURE_TYPE_PNG,
                        imageFile.getName(), Units.toEMU(500), Units.toEMU(300));

                try (FileOutputStream out = new FileOutputStream(datedOutput.resolve("report_" + (i + 1) + ".docx").toFile())) {
                    doc.write(out);
                }
            }
        }

        driver.quit();
    }
}
